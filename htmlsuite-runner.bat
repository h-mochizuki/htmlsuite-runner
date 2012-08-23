@echo off
setlocal
set RUNNER_HOME=%~dp0
"%RUNNER_HOME%gradlew" clean assemble
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