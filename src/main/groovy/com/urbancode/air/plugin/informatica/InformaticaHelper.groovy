/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

/**
 * Purpose: Provide assistance methods to the Informatica command line interface to
 * minimize the redundant code. This includes a central method to initiate the login
 * process and execute the desired command.
 */

 package com.urbancode.air.plugin.informatica;
 public class InformaticaHelper {

    final def out = System.out
    final def LS = System.getProperty("line.separator")
    final def unique = System.currentTimeMillis()
    final def inputFile = 'informatica_script.' + unique + '.in'
    final def outputFile = 'informatica_script.' + unique + '.out'
    def script = new File(inputFile)

    InformaticaHelper(def repo, def domain, def username, def password,
                                def host, def port) {
        println "[Ok] Constructing connect command..."
        script << "connect -r $repo -n $username -x $password "
        if (domain) {
            script << "-d $domain"
        }
        else {
            script << "-h $host -o $port"
        }
        println "[Ok] Added '${script.text}' to script..."
        script << "$LS"
    }

    public void addCommand2Script(def command) {
        println "[Ok] Added '${command}' to script..."
        script << command
        script << "$LS"
    }

    public void addExit2Script() {
        println "[Ok] Added 'exit' to script..."
        script << "exit"
    }

    public void printScript() {
        println("========================================")
        println("[Ok] Full Script Content:")
        script.eachLine { line -> println(line) }
        println("========================================")
    }

    public void runScript() {
        println "[Ok] Constructing runner command command..."
        def command = []
        command.add("pmrep")
        command.add("run")
        command.add("-o")
        command.add(outputFile)
        command.add("-f")
        command.add(inputFile)
        command.add("-e")
        command.add("-s")

        println("[Ok] Runner Command:")
        println(command.join(" "))
        println("")

        println "[Action] Running..."
        def lastLine = ""
        def process = command.execute()
        process.consumeProcessOutput(out, out)
        process.getOutputStream().close() // close stdin
        process.waitFor()

        def output = new File(outputFile)
        Scanner sc = new Scanner(output)
        println("[Ok] PMREP Output:")
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
    }
 }
