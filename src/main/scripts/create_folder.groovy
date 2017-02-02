/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import com.urbancode.air.plugin.informatica.TextBoxParser

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

TextBoxParser tBox = new TextBoxParser()
final def repo      = stepProps['repo']
final def domain    = stepProps['domain']
final def username  = stepProps['username']
final def password  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def host      = stepProps['host']
final def port      = stepProps['port']
final def folderArray = tBox.text2StringArray(stepProps['folderArray'])
final def description = stepProps['description']
final def owner       = stepProps['owner']
final def ownerSecurityDomain = stepProps['ownerSecurityDomain']
final def sharedFolder = Boolean.valueOf(stepProps['sharedFolder'])
final def permissions = stepProps['permissions']
final def frozen      = stepProps['frozen']

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
for (folder in folderArray) {
    script << "createfolder -n $folder "

    if (description){
        script << "-d $description "
    }
    if (owner){
        script << "-o $owner "
    }
    if (ownerSecurityDomain){
        script << "-a $ownerSecurityDomain "
    }
    if (sharedFolder){
        script << "-s "
    }
    if (permissions){
        script << "-p $permissions "
    }
    if (frozen != "0"){
        script << "-f $frozen "
    }
    script << "$LS"

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
