@echo off
setlocal
set RUNNER_HOME=%~dp0
if not exist "%RUNNER_HOME%build\libs\htmlsuite-runner-1.0.jar" (
	call "%RUNNER_HOME%gradlew" assemble
	if ERRORLEVEL 1 (
		color 3F
		pause
	)
)
java -Dfile.encoding=UTF-8 -Dgroovy.file.encoding=UTF-8 -jar "%RUNNER_HOME%build\libs\htmlsuite-runner-1.0.jar" %1
rem java -jar "%RUNNER_HOME%build\libs\htmlsuite-runner-1.0.jar" %1
if ERRORLEVEL 1 (
	color 3F
	pause
)
endlocal
