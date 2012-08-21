@echo off
setlocal
set RUNNER_HOME=%~dp0
"%RUNNER_HOME%gradlew" run -Pargs=%1
if ERRORLEVEL 1 (
	color 3F
	pause
)
endlocal