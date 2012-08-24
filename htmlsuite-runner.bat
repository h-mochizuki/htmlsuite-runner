@echo off
setlocal
set RUNNER_HOME=%~dp0
call "%RUNNER_HOME%gradlew" assemble
if ERRORLEVEL 1 (
	color 3F
	pause
)
java -jar "%RUNNER_HOME%build/libs/htmlsuite-runner-1.0.jar" %1
if ERRORLEVEL 1 (
	color 3F
	pause
)
endlocal
