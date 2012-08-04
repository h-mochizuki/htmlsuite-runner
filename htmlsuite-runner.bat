@echo off
setlocal
set RUNNER_HOME=%~dp0
echo SleniumTestSuiteをまとめたXMLファイルを指定してください。
set /P XML_PATH=^>
echo %RUNNER_HOME:~0,-1%
cd /D "%RUNNER_HOME:~0,-1%"
gradlew" run -Pargs="%XML_PATH%"
endlocal