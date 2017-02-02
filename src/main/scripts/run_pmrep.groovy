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

final def commands  = stepProps['commands']
final def repo      = stepProps['repo']
final def domain    = stepProps['domain']
final def username  = stepProps['username']
final def password  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def host      = stepProps['host']
final def port      = stepProps['port']
final def infaHome = stepProps['infaHome'];

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
script << "$commands $LS"

script << "exit"
println('script content:')
script.eachLine { line -> println(line) }
println('')

def command = []
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

println('command:')
println(command.join(' '))
println('')

def procBuilder = new ProcessBuilder(command as String[]);
if (infaHome != null && infaHome != "") {
    def env = procBuilder.environment();
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
    println("With extra  Environment : ");
    println("INFA_HOME : " + env.get("INFA_HOME"));
    println("LD_LIBRARY_PATH : " + env.get("LD_LIBRARY_PATH"));
    println("LIBPATH : " + env.get("LIBPATH"));
}


def process = procBuilder.start();
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

script.delete()
output.delete()


if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
    System.exit(1)
}
else {
    System.exit(process.exitValue())
}
