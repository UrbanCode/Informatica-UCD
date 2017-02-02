/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import com.urbancode.air.AirPluginTool;
import com.urbancode.air.plugin.informatica.InformaticaHelper;


final airTool = new AirPluginTool(args[0], args[1])
final def props = airTool.getStepProperties()
final def repo      = props['repo']
final def domain    = props['domain']
final def username  = props['username']
final def password  = props['password'] ? props['password'] : props['passwordscript']
final def host      = props['host']
final def port      = props['port']

// Construct the connection command required for all Informatica calls
InformaticaHelper ih = new InformaticaHelper(repo, domain, username, password, host, port)

final def objectType     = props['objectType']
final def objectSubtype  = props['objectSubtype']
final def objectName     = props['objectName']
final def assignedUsername  = props['assignedUsername']
final def assignedGroupName = props['assignedGroupName']
final def securityDomain = props['securityDomain']
final def permission     = props['permission']

// Construct the main cli call 'AssignPermission'
def command = []

command << "AssignPermission"

command << "-o ${objectType}"

if (objectSubtype != "Null"){
    command << "-t ${objectSubtype}"
}

command << "-n ${objectName}"

if (assignedUsername){
    command << "-u ${assignedUsername}"
}
else if (assignedGroupName){
    command << "-g ${assignedGroupName}"
}
else {
    println "[Error] Username or Group Name must be assigned to the object."
    System.exit(1)
}

if (securityDomain){
    command << "-s ${securityDomain}"
}

command << "-p ${permission}"

// Add 'AssignPermission' to the runner script
ih.addCommand2Script(command.join(" "))

// End the runner script
ih.addExit2Script()

// Print full script to screen
ih.printScript()

// Run and decide exit status
ih.runScript()
