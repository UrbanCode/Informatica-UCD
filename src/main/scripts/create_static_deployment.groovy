/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

final def out = System.out
final def LS = System.getProperty("line.separator")
final def unique = System.currentTimeMillis()

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

final def groupname = stepProps['groupname']
final def fileName  = stepProps['objectFile']
final def folder    = stepProps['folder']
final def repo      = stepProps['repo']
final def domain    = stepProps['domain']
final def username  = stepProps['username']
final def password  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def host      = stepProps['host']
final def port      = stepProps['port']
final def workflows = stepProps['workflows']

final def inputFile = 'informatica_script.' + unique + '.in'
final def outputFile = 'informatica_script.' + unique + '.out'

def script = new File(inputFile)
script << "connect -r $repo -n $username -x $password "
if (domain) {
    script << "-d $domain $LS"
}
else {
    script << "-h $host -o $port $LS"
}
script << "createdeploymentgroup -p $groupname -c $folder $LS"
if (workflows) {
    for (def workflow in workflows.split("\\s")) {
        script << "addtodeploymentgroup -p $groupname -n $workflow -o workflow -f $folder -d all $LS"
    }
}
if (fileName) {
    def objectFile = new File(fileName)
    if (objectFile.exists()) {
        // read the file and add each object to the deployment group
        objectFile.eachLine() { objectEntry ->
            def objectData = objectEntry.split(",")
            if (objectData.length == 7 && "non-reusable".equalsIgnoreCase(objectData[6])) {
                // ignore them
            }
            else if ("scheduler".equalsIgnoreCase(objectData[3])) {
                // do nothing
            }
            else if ("task".equalsIgnoreCase(objectData[3])) {
                // do nothing
            }
            else if ("transformation".equalsIgnoreCase(objectData[3])) {
                //script << "addtodeploymentgroup -p $groupname -n ${objectData[2]} -o ${objectData[3]} -t ${objectData[4]} -f $folder -d all $LS"
            }
            else {
                script << "addtodeploymentgroup -p $groupname -n ${objectData[2]} -o ${objectData[3]} -f ${objectData[1]} -d all $LS"
            }
        }
    }
}

script << "exit"

println('script content:')
script.eachLine { line -> println(line) }
println('')

def command = []
command.add('pmrep')
command.add('run')
command.add('-o')
command.add(outputFile)
command.add('-f')
command.add(inputFile)
command.add('-e')
command.add('-s')

println('command:')
println(command.join(' '))
println('')

def lastLine = ""
def process = command.execute()
process.consumeProcessOutput(out, out)
process.getOutputStream().close() // close stdin
process.waitFor()

def output = new File(outputFile)
Scanner sc = new Scanner(output)
println('pmrep output:')
while (sc.hasNextLine()) {
    lastLine = sc.nextLine()
    println(lastLine)
}
println('')
sc.close()

script.delete()
output.delete()

if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
    System.exit(1)
}
else {
    System.exit(process.exitValue())
}
