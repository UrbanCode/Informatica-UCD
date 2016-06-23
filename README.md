# Informatica-UCD
---
Note: This is not the plugin distributable! This is the source code. To find the installable plugin, go into the 'Releases' tab, and download a stable version.

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
    ValidateDeployment Group


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
