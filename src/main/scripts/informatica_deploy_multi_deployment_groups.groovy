/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import com.urbancode.air.CommandHelper
import com.urbancode.air.plugin.informatica.TextBoxParser

TextBoxParser tBox = new TextBoxParser()

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

final def groupname  = tBox.text2StringArray(stepProps['groupname'])
final def fileName  = stepProps['groupnameInputFile']
final def folder     = tBox.text2StringArray(stepProps['folder'])
final def folderDest = tBox.text2StringArray(stepProps['folderDest'])
final def label      = stepProps['label']
final def copydeploymentgroup = Boolean.valueOf(stepProps['copydeploymentgroup']) ? "YES" : "NO"
final def clearsrcdeploygroup = Boolean.valueOf(stepProps['clearsrcdeploygroup']) ? "YES" : "NO"
final def srcrepo      = stepProps['srcrepo']
final def srcdomain    = stepProps['srcdomain']
final def srcsecurityDomain = stepProps['srcsecurityDomain']
final def srcusername  = stepProps['srcusername']
final def srcpassword  = stepProps['srcpassword'] ? stepProps['srcpassword'] : stepProps['srcpasswordscript']
final def srchost      = stepProps['srchost']
final def srcport      = stepProps['srcport']

final def tarrepo      = stepProps['repo']
final def tarusername  = stepProps['username']
final def tarpassword  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def tardomain    = stepProps['tardomain']
final def tarsecurityDomain = stepProps['tarsecurityDomain']
final def tarhost      = stepProps['host']
final def tarport      = stepProps['port']
final def infaHome     = stepProps['infaHome']
final def inputFile = 'informatica_script.' + unique + '.in'
final def outputFile = 'informatica_script.' + unique + '.out'

def srcFolderNameList = ""
def process
def objectFile = new File(fileName)
def controlFile = []
File control

//List all the deploymentgroup
def script = new File(inputFile)
	script << "connect -r $tarrepo -n $tarusername -x $tarpassword "
//09/06/17 - Added LDAP based source system login
if (srcsecurityDomain) {
	script << "-s $tarsecurityDomain "
}
if (srcdomain) {
	script << "-d $tardomain $LS"
}
else {
	script << "-h $tarhost -o $tarport $LS"
}
	script << "listobjects -o DeploymentGroup  $LS "
	
	
//run the Informatica command
def command = []
def exitCode = 0;
if (infaHome != null && infaHome != "") {
	command.add(infaHome + File.separator + "server" + File.separator + "bin" + File.separator + "pmrep");
}
else {
	command.add('pmrep')
}
command.add('run')
command.add('-o')
command.add(outputFile)
command.add('-f')
command.add(inputFile)
command.add('-e')
command.add('-s')

def procBuilder = new ProcessBuilder(command as String[])
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

println("With extra  Environment : ");
println("INFA_HOME : " + env.get("INFA_HOME"));
println("LD_LIBRARY_PATH : " + env.get("LD_LIBRARY_PATH"));
println("LIBPATH : " + env.get("LIBPATH"));


procBuilder.directory
process = procBuilder.start(); command.execute()
process.consumeProcessOutput(out, out)
process.getOutputStream().close()
process.waitFor()

def output = new File(outputFile)
Scanner sc = new Scanner(output)
lastLine = ""
while (sc.hasNextLine()) {
	lastLine = sc.nextLine()
}

Scanner sa = new Scanner(output)
List<String> deploymentList = new ArrayList<String>();
while (sa.hasNextLine()){
	deploymentList.add(sa.nextLine().trim());
}

output.delete()
script.delete()


int startDeployment = 0;
int endDeployment = 0;

startDeployment = deploymentList.indexOf("listobjects -o DeploymentGroup")
endDeployment = deploymentList.indexOf(".listobjects completed successfully.")

deployList = deploymentList.subList(++startDeployment,endDeployment)

println('deployment group List:------------------------------------------------------------------')
deployList.each { line -> println "${line}" }
println('----------------------------------------------------------------------------------------')



//List all the targetfolders
script = new File(inputFile)
	script << "connect -r $tarrepo -n $tarusername -x $tarpassword "
//09/06/17 - Added LDAP based source system login
if (tarsecurityDomain) {
	script << "-s $tarsecurityDomain "
}
if (tardomain) {
	script << "-d $tardomain $LS"
}
else {
	script << "-h $tarhost -o $tarport $LS"
}
script << "listobjects -o folder  $LS "

process = procBuilder.start(); command.execute()
process.consumeProcessOutput(out, out)
process.getOutputStream().close()
process.waitFor()

output = new File(outputFile)
sc = new Scanner(output)
lastLine = ""
while (sc.hasNextLine()) {
	lastLine = sc.nextLine()
	
}

sa = new Scanner(output)
List<String> tarfolderList = new ArrayList<String>();
while (sa.hasNextLine()){
	tarfolderList.add(sa.nextLine().trim());
}

int tarstartIndex = 0;
int tarendIndex = 0;

tarstartIndex = tarfolderList.indexOf("listobjects -o folder")
tarendIndex = tarfolderList.indexOf(".listobjects completed successfully.")

def tarFolderNameList = tarfolderList.subList (++tarstartIndex,tarendIndex)

println('targetFolder List:--------------------------------------------------------')
tarFolderNameList.each { line -> println "${line}" }
println('==========================================================================')

output.delete()
script.delete()


//List all the sourcefolders
script = new File(inputFile)
script << "connect -r $srcrepo -n $srcusername -x $srcpassword "

//09/06/17 - Added LDAP based source system login
if (srcsecurityDomain) {
	script << "-s $srcsecurityDomain "
}
if (srcdomain) {
	script << "-d $srcdomain $LS"
}
else {
	script << "-h $srchost -o $srcport $LS"
}
script << "listobjects -o folder  $LS "

process = procBuilder.start(); command.execute()
process.consumeProcessOutput(out, out)
process.getOutputStream().close()
process.waitFor()

output = new File(outputFile)
sc = new Scanner(output)
lastLine = ""
while (sc.hasNextLine()) {
	lastLine = sc.nextLine()
	
}

sa = new Scanner(output)
List<String> folderList = new ArrayList<String>();
while (sa.hasNextLine()){
	folderList.add(sa.nextLine().trim());
}

int startIndex = 0;
int endIndex = 0;

startIndex = folderList.indexOf("listobjects -o folder")
endIndex = folderList.indexOf(".listobjects completed successfully.")

srcFolderNameList = folderList.subList (++startIndex,endIndex)

println('sourcefolder List:--------------------------')
srcFolderNameList.each { line -> println "${line}" }
println('============================================')

List<String> deploy = new ArrayList<String>();

for (i=0; i<deployList.size(); i++){
	while(i<deployList.size() && deployList[i].startsWith('deployment_group')) {
		deploy += deployList[i].minus('deployment_group').trim()
		i++
	}
}

script.delete()
output.delete()


def commonfolderList = tarFolderNameList.intersect(srcFolderNameList);


println('commonfolderlist List:----------------------')
commonfolderList.each { line -> println "${line}" }
println('============================================')

def commonGroup = false
def commonGroupNameList = false


List<String> groupList = new ArrayList<String>();

if (objectFile.exists()) {
	sa = new Scanner(objectFile)
	while (sa.hasNextLine()){
		groupList.add(sa.nextLine().trim());

	}
	commonGroup = groupList.any{deploy.contains(it)}
	
} else if (groupname){
	commonGroupNameList = groupname.any{deploy.contains(it)}
	
}


// Genarate control file.
//rws - leaving out attribute DEFAULTSERVERNAME="dg_sun_71099", not sure what the value should be
def controlStart = """<DEPLOYPARAMS
    COPYDEPENDENCY="YES"
    COPYDEPLOYMENTGROUP="${copydeploymentgroup}"
    COPYMAPVARPERVALS="YES"
    COPYPROGRAMINFO="YES"
    COPYWFLOWSESSLOGS="NO"
    COPYWFLOWVARPERVALS="YES"
    LATESTVERSIONONLY="YES"
    RETAINGENERATEDVAL="YES"
    RETAINSERVERNETVALS="YES">
  <DEPLOYGROUP CLEARSRCDEPLOYGROUP="${clearsrcdeploygroup}"> """

// If folderDest is empty, use source folder names as destination names
if (!folderDest) {
	folderDest = folder

}else if (!folder){
	folder = folderDest
}

if (folderDest != null && folder != null) {
	if (folderDest.size() != folder.size()){
			throw new Exception ("Enter the same number folders in both Source and Destination Informatica Folder properties.")
	}
}
def controlBottom = """</DEPLOYGROUP>
	</DEPLOYPARAMS>"""

//generate control file if input field not null 
if (groupname){
	//folder & folderdestination fileds empty generate control file.
	if(!folder && !folderDest) {
		def controlXmlFolder = ""
		def controlFolderNameList = ""

		for (int j = 0; j<commonfolderList.size(); j++){
			controlFolderNameList +=
					"""<OVERRIDEFOLDER SOURCEFOLDERNAME="${commonfolderList[j]}" SOURCEFOLDERTYPE="LOCAL"
			TARGETFOLDERNAME="${commonfolderList[j]}" TARGETFOLDERTYPE="LOCAL" MODIFIEDMANUALLY="YES"/>"""
		}

		for (int i = 0; i<groupname.size(); i++){
			def filename = "${groupname[i]}"+ unique + '.xml'
			controlFile += filename
			control = new File(filename)

			//depolyment group exist & label is empty
			if(commonGroupNameList == true && commonGroupNameList != null && label.empty ){
				controlXmlFolder = controlStart +'\n'+"""<REPLACEDG DGNAME="${groupname[i]}"></REPLACEDG>"""+'\n'+ controlFolderNameList +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group exist & label is not empty
			}else if(commonGroupNameList == true && commonGroupNameList != null && !label.empty){
				controlXmlFolder = controlStart +'\n'+ """<REPLACEDG DGNAME="${groupname[i]}"></REPLACEDG>"""+'\n'+ controlFolderNameList +'\n'+"""<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" + controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if (deployList.empty && label.empty){
				controlXmlFolder = controlStart +'\n'+ controlFolderNameList +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if(deployList.empty && !label.empty){
				controlXmlFolder = controlStart + '\n'+ controlFolderNameList +'\n'+ """<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" +'\n'+ controlBottom
				control << controlXmlFolder
			}
			if (!control.exists()){
				control.createNewFile()
				println('controlfile:' + control)
			}
		}
	} else {
		//folder & folderdestination fileds not empty generate control file..
		def controlFolder = ""
		def controlXmlFolder = ""
		for (int j = 0; j<folder.size(); j++){
			controlFolder +=
					"""<OVERRIDEFOLDER SOURCEFOLDERNAME="${folder[j]}" SOURCEFOLDERTYPE="LOCAL" 
			TARGETFOLDERNAME="${folderDest[j]}" TARGETFOLDERTYPE="LOCAL" MODIFIEDMANUALLY="YES"/>"""
		}

		for (int i = 0; i<groupname.size(); i++){
			def filename = "${groupname[i]}"+ unique + '.xml'
			controlFile += filename
			control = new File(filename)

			//depolyment group exist & label is empty
			if(commonGroupNameList == true && commonGroupNameList != null && label.empty ){
				controlXmlFolder = controlStart +'\n'+"""<REPLACEDG DGNAME="${groupname[i]}"></REPLACEDG>"""+'\n'+ controlFolder +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group exist & label is not empty
			}else if(commonGroupNameList == true && commonGroupNameList != null && !label.empty){
				controlXmlFolder = controlStart +'\n'+ """<REPLACEDG DGNAME="${groupname[i]}"></REPLACEDG>"""+'\n'+ controlFolder +'\n'+"""<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" + controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if (deployList.empty && label.empty){
				controlXmlFolder = controlStart +'\n'+ controlFolder +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if(deployList.empty && !label.empty){
				controlXmlFolder = controlStart + '\n'+ controlFolder +'\n'+ """<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" +'\n'+ controlBottom
				control << controlXmlFolder
			}
			if (!control.exists()){
				control.createNewFile()
				println('controlfile:' + control)
			}
		}
	}
//generate control file if groupnameInputFile not null
}else if(fileName){

	//folder & folderdestination fileds empty generate control file..
	if(!folder && !folderDest) {
		def controlXmlFolder = ""
		def controlFolderNameList = ""

		for (int j = 0; j<commonfolderList.size(); j++){
			controlFolderNameList +=
					"""<OVERRIDEFOLDER SOURCEFOLDERNAME="${commonfolderList[j]}" SOURCEFOLDERTYPE="LOCAL"
			TARGETFOLDERNAME="${commonfolderList[j]}" TARGETFOLDERTYPE="LOCAL" MODIFIEDMANUALLY="YES"/>"""
		}

		for (int i = 0; i<groupList.size(); i++){
			def filename = "${groupList[i]}"+ unique + '.xml'
			controlFile += filename
			control = new File(filename)

			//depolyment group exist & label is empty
			if(commonGroup == true && commonGroup != null && label.empty ){
				controlXmlFolder = controlStart +'\n'+"""<REPLACEDG DGNAME="${groupList[i]}"></REPLACEDG>"""+'\n'+ controlFolderNameList +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group exist & label is not empty
			}else if(commonGroup == true && commonGroup != null && !label.empty){
				controlXmlFolder = controlStart +'\n'+ """<REPLACEDG DGNAME="${groupList[i]}"></REPLACEDG>"""+'\n'+ controlFolderNameList +'\n'+"""<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" + controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if (deployList.empty && label.empty){
				controlXmlFolder = controlStart +'\n'+ controlFolderNameList +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if(deployList.empty && !label.empty){
				controlXmlFolder = controlStart + '\n'+ controlFolderNameList +'\n'+ """<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" +'\n'+ controlBottom
				control << controlXmlFolder
			}
			if (!control.exists()){
				control.createNewFile()
				println('controlfile:' + control)
			}
		}
	} else {
	
		//folder & folderdestination fileds not empty generate control file..
		def controlFolder = ""
		def controlXmlFolder = ""
		for (int j = 0; j<folder.size(); j++){
			controlFolder +=
					"""<OVERRIDEFOLDER SOURCEFOLDERNAME="${folder[j]}" SOURCEFOLDERTYPE="LOCAL" 
			TARGETFOLDERNAME="${folderDest[j]}" TARGETFOLDERTYPE="LOCAL" MODIFIEDMANUALLY="YES"/>"""
		}

		for (int i = 0; i<groupList.size(); i++){
			def filename = "${groupList[i]}"+ unique + '.xml'
			controlFile += filename
			control = new File(filename)

			//depolyment group exist & label is empty
			if(commonGroup == true && commonGroup != null && label.empty ){
				controlXmlFolder = controlStart +'\n'+"""<REPLACEDG DGNAME="${groupList[i]}"></REPLACEDG>"""+'\n'+ controlFolder +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group exist & label is not empty
			}else if(commonGroup == true && commonGroup != null && !label.empty){
				controlXmlFolder = controlStart +'\n'+ """<REPLACEDG DGNAME="${groupList[i]}"></REPLACEDG>"""+'\n'+ controlFolder +'\n'+"""<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" + controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is empty
			}else if (deployList.empty && label.empty){
				controlXmlFolder = controlStart +'\n'+ controlFolder +'\n'+ controlBottom
				control << controlXmlFolder

				//depolyment group not exist & label is not empty
			}else if(deployList.empty && !label.empty){
				controlXmlFolder = controlStart + '\n'+ controlFolder +'\n'+ """<APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO" TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>""" +'\n'+ controlBottom
				control << controlXmlFolder
			}
			if (!control.exists()){
				control.createNewFile()
				println('controlfile:' + control)
			}
		}
	}
}


//Multiple Deployment Groups supplied via input file
if (fileName) {
	if (groupList != null && groupList.size() > 0) {
		for (int i = 0; i < groupList.size(); i++) {
			script << "deploydeploymentgroup -p \"${groupList[i]}\"  -c \"${controlFile[i]}\"  -r $tarrepo -n $tarusername -x $tarpassword "

			if (tarsecurityDomain) {
				script << "-s $tarsecurityDomain "
			}

			if (tardomain) {
				script << "-d $tardomain $LS"
			}
			else {
				script << "-h $tarhost -o $tarport $LS"
			}
		}
	}
}

//Multiple Deployment Groups  via input field
if (groupname){
	for (int i = 0; i < groupname.size(); i++) {
		script << "deploydeploymentgroup -p \"${groupname[i]}\" -c \"${controlFile[i]}\" -r $tarrepo -n $tarusername -x $tarpassword "

		if (tarsecurityDomain) {
			script << "-s $tarsecurityDomain "
		}

		if (tardomain) {
			script << "-d $tardomain $LS"
		}
		else {
			script << "-h $tarhost -o $tarport $LS"
		}

	}
}
script << "exit"

println('script content:')
script.eachLine { line -> println(line) }
println('')

process = procBuilder.start();
process.consumeProcessOutput(out, out)
process.getOutputStream().close() // close stdin
process.waitFor()

sc = new Scanner(output)
println('pmrep output:------------------------------')
def lastLine = ""
while (sc.hasNextLine()) {
	lastLine = sc.nextLine()
	println(lastLine)
}

sc.close()
script.delete()
output.delete()

if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
	System.exit(1)
}
else {
	System.exit(process.exitValue())
}
