README for the AGT-Detector Project

Folder Structure
===================
We plan to use a simple folder structure with initially 3 folders
- agt: Classes that are buyilding blocks for our programs
- agt.main: Program entry points (Main classes)
- agt.trying_things: Temporary stuff that might be removed at any point

The config Folder
=================
A folder containg files used to configure the system. For example, Weka models 
and user-to-profile data.

Properties
==========
Properties are saved in the resources folder. We have a workspace specific property 
file (agt.properties) that defines entities and points to resources within the workspace.
Each one of us is also expected to have another property file (local.properties) that 
that defines entities and points to resources on our own computers. For example, a property 
ntsDir gives the path to our own NTS tweet folder, and twitterKeys gives the path to a property 
file with ourown Twitter API keys.
