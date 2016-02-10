Note: This plug-in is currently a beta release. The functionality is subject to change. This information is for planning purposes only. The information herein is subject to change or removal without notice before the products described may become available.
Overview

Overview

Informatica specializes in data management and gives their users powerful methods to access this data through Informatica Power Center’s Client Tools, Repositories, and Servers.

The UCD Informatica Plugin is an automation plugin that connects to specific repositories and directly access and merge data across remote systems within your organization.

The Informatica plug-in includes the following steps:

    Apply Label
    Create Dynamic Deployment Group
    Create Folder
    Create Static Deployment Group
    Deploy Deployment Group
    Import Objects
    Roll Back Deployment Group
    Run PMREP Command
    ValidateDeployment Group


Compatibility
	The IBM UrbanCode Deploy automation plug-in works with Informatica Version 9.6 and later.
	This plug-in requires version 6.1.1 or later of IBM UrbanCode Deploy.

Installation
	The packaged zip is located in the dist folder. No special steps are required for installation.
	See Installing plug-ins in UrbanCode Deploy. Download this zip file if you wish to skip the 
	manual build step. Otherwise, download the entire uDeploy-Informatica-Plugin and 
	run the "ant" command in the top level folder. This should compile the code and create
	a new distributable zip within the dist folder. Use this command if you wish to make
	your own changes to the plugin.
History
	Version 10 (Initial Beta Release)

The following features are included in the initial beta release of the plug-in:
    Create Static and Dynamic Deployment Groups
    Deploy, Validate, and Roll Back Deployment Groups
    Apply labels to objects in a target server

	Version 11 (Update #1)

The following features are included in the initial beta release of the plug-in:
    Create Folder step
