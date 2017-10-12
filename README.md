# IBM UrbanCode Deploy Informatica Plugin [![Build Status](https://travis-ci.org/IBM-UrbanCode/Informatica-UCD.svg?branch=master)](https://travis-ci.org/IBM-UrbanCode/Informatica-UCD)
---
Note: This is not the plugin distributable! This is the source code. To find the installable plugin, go into the 'Releases' tab, and download a stable version.

### License
This plugin is protected under the [Eclipse Public 1.0 License](http://www.eclipse.org/legal/epl-v10.html)

### Overview

Informatica specializes in data management and gives their users powerful methods to access this data through Informatica Power Center’s Client Tools, Repositories, and Servers.

The UCD Informatica Plugin is an automation plugin that connects to specific repositories and directly access and merge data across remote systems within your organization.

### Steps:

    Apply Label
    Assign Permission
    Create Dynamic Deployment Group
    Create Folder
    Create Static Deployment Group
    Deploy Deployment Group
    Import Objects
    Roll Back Deployment Group
    Run PMREP Command
    Validate Deployment Group


### Compatibility
	The IBM UrbanCode Deploy automation plug-in works with Informatica Version 9.6 and later.
	This plug-in requires version 6.1.1 or later of IBM UrbanCode Deploy.

### Installation
	The packaged zip is located in the dist folder. No special steps are required for installation.
	See Installing plug-ins in UrbanCode Deploy. Download this zip file if you wish to skip the
	manual build step. Otherwise, download the entire Information-UCD and
	run the "ant" command in the top level folder. This should compile the code and create
	a new distributable zip within the dist folder. Use this command if you wish to make
	your own changes to the plugin.
### History
    Version 25
        Issue #26 - Security Domain property added to Apply Label step.
    Version 24
        Issue #18 - Type property added to the Create Dynamic Deployment step.
        Issue #19 - INFA HOME property added to the Deploy Deployment Group step.
        INFA_HOME, LD_LIBRARY_PATH, and LIBPATH environment variables are set in the Create Deploy Deployment and Deploy Deployment Group steps.
        Removed extra execute command from the Validate Deployment step.
    Version 23
        Import Objects step no longer adds Retain Generated Value and Checkin After Import parameters to control file if not selected.
    Version 22
        The Roll Back Deployment Group Step properly uses the -t flag.
    Version 21
        Added INFA HOME property to the Create Dynamic Deployment Group step.
    Version 20
        The following update has been made to the Validate Deployment Group step:
        - Added Security Domain, LANG, and INFAHOME environment properties.
        Fixed security vulnerability in import_objects.groovy: On exit, the generated script file will be deleted.
    Version 19
        The following update was been made to the Import Objects step:
        - Fixed Import Object's security domain line seperation so that it does not bleed into the next variable or create a new line in the connection string.
    Version 18
        The following updates have been made to the Import Objects step:
        - Added a new line to prevent the Security Domain argument from bleeding into the following line.
        - Now able to specify files paths for the Folder Mapping, Repository Mapping, and Conflict Resolution Rules list properties.
    Version 17
        Added security domain property to step "Import Objects" to fix authentication problem
        Added LANG property to step "Import Objects" to fix encoding problem
        All default property references are now optional.
    Version 16
        Updated plugin-groovy-utils to v1.2.
        Updated build.xml and plugin folder structure.
    Version 15
        Added the following functionality and updates:
        - Revamped the build.xml to no longer use plugin-build.xml. Now uses Apache Ivy to download dependencies.
        - Added groovy-plugin-utils-1.0.jar library.
        - Runs successfully without extra libs or jars.
        - Added the Copy Deployment Group (COPYDEPLOYMENTGROUP) pmrep control file property to Deploy Deployment Group step.
        - Removed the "launch..." scripts, added classes and groovy-plug-utils-1.0.jar to all classpaths.
        - Added EPL license.
    Version 14
        Added the Assign Permission step.
    Version 13
        Import Objects Step Updates:
        - Default Conflict Resolution property is now a selectBox with RENAME, REUSE, and REPLACE options.
        - On failure, the step will now terminate correctly. The stacktrace will be printed for debugging purposes.
    Version 12
        Create Folder step now correctly uses the -shared folder argument.
	Version 11
		The following feature is included in version 11 of the Informatica plug-in:
		Implemented the PMREP command "Create Folder".
	Version 10
		The following features are included in first update to Informatica plug-in:
		Added "Destination Informatica Folder(s)" property to the Deploy Deployment Group step.
		Renamed "Informatica Folder(s)" to "Source Informatica Folders(s)" for clarity.
		Updated "Source Informatica Folder(s)" description to reflect these changes.
	Version 9
		The following features are included in the initial release of the plug-in:
		Create static and dynamic deployment groups
		Deploy, validate, and roll back deployment groups
		Apply labels to objects in a target server
		Import objects from XML files with necessary connect and exit commands and control files
