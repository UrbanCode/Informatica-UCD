/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import com.urbancode.air.CommandHelper
import com.urbancode.air.plugin.informatica.TextBoxParser


TextBoxParser tBox = new TextBoxParser();

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

final def srcfolder    = tBox.text2StringArray(stepProps['srcfolder'])
final def tarfolder    = tBox.text2StringArray(stepProps['tarfolder'])
final def commands     = stepProps['commands']
final def srcrepo      = stepProps['srcrepo']
final def srcdomain    = stepProps['srcdomain']
final def srcusername  = stepProps['srcusername']
final def srcpassword  = stepProps['srcpassword']
final def srchost      = stepProps['srchost']
final def srcport      = stepProps['srcport']
final def infaHome = stepProps['infaHome'];
final def srcsecurityDomain = stepProps['srcsecurityDomain']
final def tardomain    = stepProps['tardomain']
final def tarsecurityDomain = stepProps['tarsecurityDomain']
final def tarrepo      = stepProps['tarrepo']
final def tarusername  = stepProps['tarusername']
final def tarpassword  = stepProps['tarpassword']
final def tarhost      = stepProps['tarhost']
final def tarport      = stepProps['tarport']

final def inputFile = 'informatica_script.' + unique + '.in'
final def outputFile = 'informatica_script.' + unique + '.out'

def controlFile = []
def process

// generate control file
def controlStart = """<!DOCTYPE DEPLOYPARAMS SYSTEM "/u01/app/Dev/Informatica/pwc961hf2/server/bin/depcntl.dtd">
	<DEPLOYPARAMS
	CHECKIN_COMMENTS= \"${new Date().toString()}\"
	COPYPROGRAMINFO = "YES"
	COPYMAPVARPERVALS = "YES"
	COPYWFLOWVARPERVALS = "NO"
	COPYWFLOWSESSLOGS = "NO"
	COPYDEPENDENCY = "YES"
	LATESTVERSIONONLY = "NO"
	RETAINGENERATEDVAL = "YES"
	RETAINSERVERNETVALS = "YES"
	RETAINMAPVARPERVALS = "YES">
  <DEPLOYFOLDER>"""


// If targetfolder  is empty, use source folder names as targetfolder names
  if (!tarfolder) {
	  tarfolder = srcfolder
  }

// Fail if the array sizes are not the same
  else if (tarfolder.size() != srcfolder.size()){
	  throw new Exception ("Enter the same number folders in both Source and Target Informatica Folder properties.")
  }

  for (int i = 0; i < tarfolder.size(); i++) {
	  def filname = "${tarfolder[i]}_" + unique  + i + '.xml'
		  controlFile += filname
		  File control = new File(filname)
  if (!control.exists()){
		  control.createNewFile()
  }

  def controlXml = controlStart + '\n' +
			"""<REPLACEFOLDER
    FOLDERNAME=\"${tarfolder[i]}\"
    RETAINMAPVARPERVALS = "YES"
    RETAINWFLOWVARPERVALS = "YES"
    RETAINWFLOWSESSLOGS = "NO"
    MODIFIEDMANUALLY = "NO"
    RETAINORIGFOLDEROWNER = "YES"/> """ + """
	</DEPLOYFOLDER>
	</DEPLOYPARAMS>"""

		control << controlXml
  	}

  	  for (int i = 0; i < controlFile.size(); i++){
	 		File control = new File(controlFile[i])
	  if (control.exists()){
	  
	  		println('controlfile:' + control)

  			}

  		}

// script for folder deployment		
	def script = new File(inputFile)
		script << "connect -r $srcrepo -n $srcusername -x $srcpassword "

	if (srcsecurityDomain) {
		script << "-s $srcsecurityDomain "

	}
	if (srcdomain) {
		script << "-d $srcdomain $LS"

	} else {
		script << "-h $srchost -o $srcport $LS"

	}
	for (int i = 0; i < srcfolder.size(); i++) {
		script << "deployfolder -f \"${srcfolder[i]}\"  -c \"${controlFile[i]}\" -r $tarrepo -n $tarusername -x $tarpassword "

	if (tarsecurityDomain) {
		script << "-s $tarsecurityDomain "
	}
	if (tardomain) {
		script << "-d $tardomain $LS"

	} else {
		script << "-h $tarhost -o $tarport $LS"

		}

	}
		script << "exit"
	
  	  println('Deploy folder script content:' + script )
	  script.eachLine { line -> println(line) }
	  println('')

	//run the Informatica command
	def exitCode = 0
	def command = []
	  if (infaHome != null && infaHome != "") {
		  command.add(infaHome + File.separator + "server" + File.separator + "bin" + File.separator + "pmrep");
	  } else {
	  	  command.add('pmrep')
	  }

	  	  command.add('run')
		  command.add('-o')
		  command.add(outputFile)
		  command.add('-f')
		  command.add(inputFile)
		  command.add('-e')
		  command.add('-s')

		  println('command:' + command )
		  println(command.join(' '))
		  println('')

	def procBuilder = new ProcessBuilder(command as String[]);
		if (infaHome != null && infaHome != "") {
			def env = procBuilder.environment();
				env.put("INFA_HOME", infaHome);

		if (env.get("LD_LIBRARY_PATH") != null && env.get("LD_LIBRARY_PATH") != "") {
				env.put("LD_LIBRARY_PATH", env.get("LD_LIBRARY_PATH") + File.pathSeparator + infaHome + File.separator + "server" + File.separator + "bin");
		} else {
				env.put("LD_LIBRARY_PATH", infaHome + File.separator + "server" + File.separator + "bin");
		}
		
		if (env.get("LIBPATH") != null && env.get("LIBPATH") != "") {
				env.put("LIBPATH", env.get("LIBPATH") + File.pathSeparator + infaHome + File.separator + "server" + File.separator + "bin");
		}else {
				env.put("LIBPATH", infaHome + File.separator + "server" + File.separator + "bin");
		}

			println("With extra  Environment : ");
			println("INFA_HOME : " + env.get("INFA_HOME"));
			println("LD_LIBRARY_PATH : " + env.get("LD_LIBRARY_PATH"));
			println("LIBPATH : " + env.get("LIBPATH"));
		}

			process = procBuilder.start();
			process.consumeProcessOutput(out, out)
			process.getOutputStream().close() // close stdin
			process.waitFor()
					
			
		def output = new File(outputFile)
			Scanner sc = new Scanner(output)
			println('..........pmrep Deployfolder output:...........')
		def lastLine = ""
			while (sc.hasNextLine()) {
				lastLine = sc.nextLine()
			println(lastLine)
		}
			println('')
					
			sc.close();		
			
		for (int i = 0; i < srcfolder.size(); i++) {
			File control = new File(controlFile[i])
		if (control.exists()){
			control.delete()
			}

		}
		
		script.delete()
		output.delete()
	

		if (!lastLine || !lastLine.trim().equalsIgnoreCase("exit")) {
			System.exit(1)
			
		} else {
			exitCode = process.exitValue()
		}
		

