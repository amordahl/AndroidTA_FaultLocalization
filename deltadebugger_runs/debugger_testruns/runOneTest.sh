#!/bin/bash

echo "Running Single Test"

java -jar ViolationDeltaDebugger-1.0-SNAPSHOT-jar-with-dependencies.jar -violation debugger_testruns/flowset_violation-True_Callbacks_Button2.apk.xml -root_projects $1 -p singleTestRun -c -hdd -no_opt
