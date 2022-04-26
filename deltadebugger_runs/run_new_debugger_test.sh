rm -r -f Button2_test_proj/
cp -r ~/droidbench_android_projects/Button2/ Button2_test_proj/
./Button2_test_proj/gradlew assembleDebug -p Button2_test_proj/
java -jar ViolationDeltaDebugger-1.0-SNAPSHOT-jar-with-dependencies.jar Button2_test_proj/ test_build_script.sh test_violation_script.sh -hdd
