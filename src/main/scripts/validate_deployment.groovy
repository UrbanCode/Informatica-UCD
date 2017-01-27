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

final def query     = stepProps['query']
final def validationOutputFileName = stepProps['validationFile']
final def repo      = stepProps['repo']
final def domain    = stepProps['domain']
final def username  = stepProps['username']
final def password  = stepProps['password'] ? stepProps['password'] : stepProps['passwordscript']
final def securityDomain = stepProps['securityDomain']
final def host      = stepProps['host']
final def port      = stepProps['port']
final def lang = stepProps['lang'];

final def inputFile = 'informatica_script.' + unique + '.in'
final def outputFile = 'informatica_script.' + unique + '.out'
final def queryOutputFileName = 'informatica_script.' + unique + '.qry'
final def infaHome = stepProps['infaHome']
def queryOutputFile = new File(queryOutputFileName)
def validationOutputFile = new File(validationOutputFileName)

println("Running query for invalid objects: $query...")

// generate Informatica script
def script = new File(inputFile)
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
script << "executequery -q $query -u $queryOutputFileName $LS"

script << "exit"
println('script content:')
script.eachLine { line -> println(line) }
println('')

//start informatica process
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

def exitCode = 0
	def procBuilder = new ProcessBuilder(command as String[])
	procBuilder.directory

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
		
def process = procBuilder.start(); command.execute()
process.consumeProcessOutput(out, out)
process.getOutputStream().close() // close stdin
process.waitFor()

def output = new File(outputFile)
def sc = new Scanner(output)
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


if (process.exitValue()) {
    println('')
    println("Error executing query for invalid objects.")
    System.exit(process.exitValue())
}
else if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
    println('')
    println("Error executing query for invalid objects.")
    System.exit(1)
}

if (queryOutputFile.exists() && queryOutputFile.length() > 0) {
    println('')
    println("Found invalid objects in the repository. Running validate on the objects.")
    println('')
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
    script << "validate -i $queryOutputFileName -s -k -m validated_$query -p saved -u $validationOutputFileName $LS"
    
    script << "exit"
    println('script content:')
    script.eachLine { line -> println(line) }
    println('')
    
    println('command:')
    println(command.join(' '))
    println('')
    
    process = procBuilder.start(); command.execute()
    process.consumeProcessOutput(out, out)
    process.getOutputStream().close() // close stdin
    process.waitFor()
    
    sc = new Scanner(output)
    println('pmrep output:')
    lastLine = ""
    while (sc.hasNextLine()) {
        lastLine = sc.nextLine()
        println(lastLine)
    }
    println('')
    sc.close()
    
    script.delete()
    output.delete()

    if (process.exitValue()) {
        println('')
        println("Error while validating Invalid/Impacted objects.")
        System.exit(process.exitValue())
    }
    else if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
        println('')
        println("Error executing query for invalid objects.")
        System.exit(1)
    }
    
}
else {
    println('')
    println("No invalid objects found in the repository.")
    println('')

}
