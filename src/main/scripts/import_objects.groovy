/**
 * © Copyright IBM Corporation 2015, 2018.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import java.security.MessageDigest

final def out = System.out
final def LS = System.getProperty("line.separator")
final def unique = System.currentTimeMillis()
def workDir = new File('.').canonicalFile

final def stepProps = new Properties();
final def inputPropsFile = new File(args[0]);
final def inputPropsStream = null;
try {
    inputPropsStream = new FileInputStream(inputPropsFile);
    stepProps.load(inputPropsStream);
}
catch (IOException e) {
    throw new RuntimeException(e);
}

final def dirOffset = stepProps['dirOffset']
final def repo      = stepProps['repo']
final def domain    = stepProps['domain']
final def username  = stepProps['username']
final def password  = stepProps['password']
if (!password || password.size() == 0) {
    password = stepProps['passwordscript']
}
final def securityDomain = stepProps['securityDomain']
final def host      = stepProps['host']
final def port      = stepProps['port']
final def infaHome = stepProps['infaHome'];
final def lang = stepProps['lang'];
final def include = stepProps['include'];
final def exclude = stepProps['exclude'];
final def folderMappingList = stepProps['folderMappingList'];
final def repositoryMappingList = stepProps['repositoryMappingList'];
final def conflictResolutionList = stepProps['conflictResolutionList'];
final def conflictResolutionDefault = stepProps['conflictResolutionDefault'];
final def retainGenSeq = Boolean.valueOf(stepProps['retainGenSeq']);
final def checkinAfterImport = Boolean.valueOf(stepProps['checkinAfterImport']);

final def inputFile = 'informatica_script.' + unique + '.sh'
final def controlFile = 'informatica_script.' + unique + '.ctl'

final def conflictRuleSet = [
        'Lookup Procedure',
        'Stored Procedure',
        'Expression',
        'Filter',
        'Aggregator',
        'Rank',
        'Normalizer',
        'Router',
        'Sequence',
        'Sorter',
        'update strategy',
        'Custom Transformation',
        'Transaction control',
        'External Procedure',
        'Joiner',
        'Mapping',
        'Mapplet',
        'Source Definition',
        'Target Definition',
        'Session',
        'Workflow',
        'Worklet',
        'Email',
        'SessionConfig'
]

def fileSet = [:]

if (dirOffset) {
    workDir = new File(workDir, dirOffset).canonicalFile
}

def encodeBase64String(def input) {
    def byteArray = Base64.encodeBase64(input, false, true)
    return new String(byteArray)
}

// search for files to process
final def ant = new AntBuilder()
def scanner = ant.fileScanner {
    if (exclude && exclude.trim().length() > 0) {
        fileset(dir: "${workDir.canonicalPath}", includes: "${include.split('\n').join(' ')}", excludes: "${exclude?.split('\n')?.join(' ')}", casesensitive: false)
    } else {
        fileset(dir: "${workDir.canonicalPath}", includes: "${include.split('\n').join(' ')}", casesensitive: false)
    }
}

if (!scanner.hasFiles()) {
    println "Did not find any files matching \"${include.split('\n').join(' ')}\" except \"${exclude?.split('\n')?.join(' ')}\" in ${workDir.canonicalPath}!"
}
else {
    for (scannedFile in scanner) {
        def oldFileName = scannedFile.getName()
        def digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update(oldFileName.bytes)
        def newXmlFileName = "${new BigInteger(1,digest.digest()).toString(16).padLeft(32, '0')}.xml"
        println ("Encoding XML file name ${oldFileName}.xml to: ${newXmlFileName}")
        def newXmlFile = new File(workDir, newXmlFileName)
        ant.copy(file: scannedFile, tofile: newXmlFile)
        fileSet << [(newXmlFile) : "${oldFileName}.xml"]
    }

    // generate Bash script with pmrep Informatica commands
    def script = new File(workDir, inputFile)
    script.deleteOnExit()

    script << "#!/bin/bash $LS";

    script.setExecutable(true);

    if (infaHome != null && infaHome != "") {
            script << infaHome + File.separator + "server" + File.separator + "bin" + File.separator + "pmrep ";
    }
    else {
            script << 'pmrep ';
    }

    script << "connect -r $repo -n $username -x $password "

    if (securityDomain){
        script << "-s ${securityDomain} "
    }

    if (domain) {
        script << "-d $domain $LS"
    }
    else {
        script << "-h $host -o $port $LS"
    }

    fileSet.keySet().each {
        if (infaHome != null && infaHome != "") {
            script << infaHome + File.separator + "server" + File.separator + "bin" + File.separator + "pmrep ";
        }
        else {
            script << 'pmrep ';
        }
        def filePrefix = it.name.substring(0, it.name.indexOf(".xml"))
        script << "ObjectImport -i \"$it\" -c \"${filePrefix}.ctl\" $LS"
    }

    println('script content:')
    script.eachLine { line -> println(line) }
    println('')

    // generate control file
    def folderMap = [:] as Map
    def repoMap = [:] as Map
    def conflictMap = [:] as Map
    def foundErrors = false
    def repoFolderSet = [] as Set

	File foldermapping = new File(folderMappingList)
	if (foldermapping.isFile()) {
		folderMappingList = foldermapping.text
}

    folderMappingList?.split('\n')?.each {
        if (it && it.trim().length() > 0) {
            def index = it.indexOf('=')
            if (index < 0 || index == (it.trim().length() - 1)) {
                foundErrors = true
                println "Found invalid folder mapping: $it"
            }
            else {
                folderMap.put(it.substring(0, index).trim(), it.substring(index + 1).trim())
            }
        }
    }
	File repositorymapping = new File(repositoryMappingList)
	if (repositorymapping.isFile()) {
		repositoryMappingList = repositorymapping.text
}

    repositoryMappingList?.split('\n')?.each {
        if (it && it.trim().length() > 0) {
            def index = it.indexOf('=')
            if (index < 0 || index == (it.trim().length() - 1)) {
                foundErrors = true
                println "Found invalid repository mapping: $it"
            }
            else {
                repoMap.put(it.substring(0, index).trim(), it.substring(index + 1).trim())
            }
        }
    }
	File conflictresolution = new File(conflictResolutionList)
	if (conflictresolution.isFile()) {
		conflictResolutionList = conflictresolution.text
}
    conflictResolutionList?.split('\n')?.each {
        if (it && it.trim().length() > 0) {
            def index = it.indexOf('=')
            if (index < 0 || index == (it.trim().length() - 1)) {
                foundErrors = true
                println "Found invalid Conflict Resolution Rule: $it"
            }
            else {
                conflictMap.put(it.substring(0, index).trim(), it.substring(index + 1).trim())
            }
        }
    }

    if (foundErrors) {
        System.exit 1
    }

    fileSet.keySet().each {
        repoFolderSet.clear()
        def filePrefix = it.name.substring(0, it.name.indexOf(".xml"))
        println ("Using control file ${filePrefix}.ctl for original XML file: ${fileSet[it]}")
        def control = new File(workDir, "${filePrefix}.ctl")
        control << '<?xml version="1.0" encoding="UTF-8"?>'
        def importParams = '<IMPORTPARAMS '
        if (retainGenSeq) {
            importParams += 'RETAIN_GENERATED_VALUE="YES" '
        }
        if (checkinAfterImport) {
            importParams += 'CHECKIN_AFTER_IMPORT="YES" '
            def sourceFolderList = folderMap.collect{it.key}.join(',').toString()
            importParams += "CHECKIN_COMMENTS=\"${sourceFolderList}: ${new Date().toString()}\" "
        }
        importParams += '>'
        control << importParams

        println "Processing $it"
        def parser = new XmlSlurper(false, false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        def xml = parser.parse(it)
        xml.'REPOSITORY'.each { repository ->
            def repoName = repository.'@NAME'.text()
            repository.'FOLDER'.each { folder ->
                def folderName = folder.'@NAME'.text()
                if (!repoFolderSet.contains(repoName + folderName)) {
                    repoFolderSet.add(repoName+folderName)
                    control << "<FOLDERMAP SOURCEFOLDERNAME=\"$folderName\" SOURCEREPOSITORYNAME=\"$repoName\" TARGETFOLDERNAME=\"${folderMap.get(folderName)?:folderName}\" TARGETREPOSITORYNAME=\"${repoMap.get(repoName)?:repoName}\"/>"
                }
            }
        }
        control << '<RESOLVECONFLICT>'

        conflictRuleSet.each {
            control << "<TYPEOBJECT OBJECTTYPENAME=\"$it\" RESOLUTION=\"${conflictMap.get(it)?:conflictResolutionDefault}\"/>"
        }

        // add any additional conflict resolutions
        conflictMap.keySet().each {
            if(!conflictRuleSet.contains(it)) {
                control << "<TYPEOBJECT OBJECTTYPENAME=\"$it\" RESOLUTION=\"${conflictMap.get(it)?:conflictResolutionDefault}\"/>"
            }
        }

        control << '</RESOLVECONFLICT>'
        control << '</IMPORTPARAMS>'
    }

    //run the Informatica command
    def command = []
    command.add(inputFile)

    println('command:')
    println(command.join(' '))
    println('')

    def exitCode = 0
    try {
        def procBuilder = new ProcessBuilder(command as String[])
        procBuilder.directory(workDir)

        def env = procBuilder.environment();
        if (infaHome != null && infaHome != "") {
            env.put("INFA_HOME", infaHome);

            if (env.get("LD_LIBRARY_PATH") != null && env.get("LD_LIBRARY_PATH") != "") {
                env.put("LD_LIBRARY_PATH", env.get("LD_LIBRARY_PATH") + File.pathSeparator + infaHome + File.separator + "server" + File.separator + "bin");
            }
            else {
                env.put("LD_LIBRARY_PATH", infaHome + File.separator + "server" + File.separator + "bin");
            }

            if (env.get("LIBPATH") != null && env.get("LIBPATH") != "") {
                env.put("LIBPATH", env.get("LIBPATH") + File.pathSeparator + infaHome + File.separator + "server" + File.separator + "bin");
            }
            else {
                env.put("LIBPATH", infaHome + File.separator + "server" + File.separator + "bin");
            }
        }

        if (lang != null && lang != "") {
            env.put("LANG", lang);
        }

        println("With extra  Environment : ");
        println("INFA_HOME : " + env.get("INFA_HOME"));
        println("LD_LIBRARY_PATH : " + env.get("LD_LIBRARY_PATH"));
        println("LIBPATH : " + env.get("LIBPATH"));
        println("LANG : " + env.get("LANG"));

        def process = procBuilder.start();
        process.consumeProcessOutput(out, out)
        process.getOutputStream().close() // close stdin
        process.waitFor()

        exitCode = process.exitValue();
    }
    catch (Exception ex) {
        println "[Error] Please review the output log and stack trace for information on the error."
        println ex.printStackTrace()
        exitCode = 1
    }
    finally {
        fileSet.keySet().each {
            it.delete()
        }
        System.exit(exitCode)
    }
}
