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

final def groupname = stepProps['groupname']
final def folder    = tBox.text2StringArray(stepProps['folder'])
final def folderDest = tBox.text2StringArray(stepProps['folderDest'])
final def label     = stepProps['label']

final def srcrepo      = stepProps['srcrepo']
final def srcdomain    = stepProps['srcdomain']
final def srcusername  = stepProps['srcusername']
final def srcpassword  = stepProps['srcpassword'] ? stepProps['srcpassword'] : stepProps['srcpasswordscript'] 
final def srchost      = stepProps['srchost']
final def srcport      = stepProps['srcport']

final def tarrepo      = stepProps['repo']
final def tarusername  = stepProps['username']
final def tarpassword  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript'] 
final def tarhost      = stepProps['host']
final def tarport      = stepProps['port']

final def inputFile = 'informatica_script.' + unique + '.in'
final def outputFile = 'informatica_script.' + unique + '.out'
final def controlFile = 'informatica_control_' + unique + '.xml'

def control = new File(controlFile)
// rws - leaving out attribute DEFAULTSERVERNAME="dg_sun_71099", not sure what the value should be
control << """<DEPLOYPARAMS
    COPYDEPENDENCY="YES"
    COPYDEPLOYMENTGROUP="YES"
    COPYMAPVARPERVALS="YES"
    COPYPROGRAMINFO="YES"
    COPYWFLOWSESSLOGS="NO"
    COPYWFLOWVARPERVALS="YES"
    LATESTVERSIONONLY="YES"
    RETAINGENERATEDVAL="YES"
    RETAINSERVERNETVALS="YES">
  <DEPLOYGROUP CLEARSRCDEPLOYGROUP="NO">"""
  
// If folderDest is empty, use source folder names as destination names
if (!folderDest) {
	folderDest = folder
}
// Fail if the array sizes are not the same
else if (folderDest.size() != folder.size()){
	throw new Exception ("Enter the same number folders in both Source and Destination Informatica Folder properties.")
}

for (int i = 0; i < folder.size(); i++) {
    control << """
    <OVERRIDEFOLDER SOURCEFOLDERNAME="${folder[i]}" SOURCEFOLDERTYPE="LOCAL"
      TARGETFOLDERNAME="${folderDest[i]}" TARGETFOLDERTYPE="LOCAL" MODIFIEDMANUALLY="YES"/>"""
}

if (label) {
    control << """
    <APPLYLABEL SOURCELABELNAME = "$label" SOURCEMOVELABEL = "NO"
      TARGETLABELNAME = "$label" TARGETMOVELABEL = "NO"/>"""
}
control << """
  </DEPLOYGROUP>
</DEPLOYPARAMS>"""

println('control content:')
control.eachLine { line -> println(line) }
println('')

def script = new File(inputFile)
script << "connect -r $srcrepo -n $srcusername -x $srcpassword "
if (srcdomain) {
    script << "-d $srcdomain $LS"
}
else {
    script << "-h $srchost -o $srcport $LS"
}
script << "deploydeploymentgroup -p $groupname -c $controlFile -r $tarrepo -n $tarusername -x $tarpassword "
// always use the host and port because the domain is likely not accessible from the source environment
//if (tardomain) {
//    script << "-d $tardomain $LS"
//}
//else {
    script << "-h $tarhost -o $tarport $LS"
//}
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

def process = command.execute()
process.consumeProcessOutput(out, out)
process.getOutputStream().close() // close stdin
process.waitFor()

def output = new File(outputFile)
Scanner sc = new Scanner(output)
println('pmrep output:')
def lastLine = ""
while (sc.hasNextLine()) {
    lastLine = sc.nextLine()
    println(lastLine)
}
println('')
sc.close()

control.delete()
script.delete()
output.delete()


if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
    System.exit(1)
}
else {
    System.exit(process.exitValue())
}
