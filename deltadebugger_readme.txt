Documentation
Main program:
1. ViolationDeltaDebugger
Requirements:
1. Droidbench android projects
2. ProjectLineCounter
3. DroidbenchProjectCreator
4. AndroidTAEnvironment
	4a. violations
	4b. flowdroid
	4c. run_aql.py
Setup:
Setup requirements:
	Droidbench android projects:
1.	clone this repo into a directory https://github.com/Pancax/droidbench_android_projects.git 
	ProjectLineCounter:
1.	First, clone this repo https://github.com/amordahl/AndroidTA_FaultLocalization.git 
2.	“git checkout origin active-dev”
3.	Mvn clean install the resources/delta_debugger/ProjectLineCounter
	DroidbenchProjectCreator:
1.	First, clone this repo https://github.com/amordahl/AndroidTA_FaultLocalization.git 
2.	“git checkout origin active-dev”
3.	Mvn clean install the resources/delta_debugger/DroidbenchProjectCreator
Setup Main program:
•	First, clone this repo https://github.com/amordahl/AndroidTA_FaultLocalization.git 
•	“git checkout origin active-dev”
•	Mvn clean package resources/delta_debugger/ViolationDeltaDebugger

Program options
