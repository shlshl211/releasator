@echo off
setlocal enableextensions enabledelayedexpansion

REM Variable RELEASATOR_JAVA: you may wish to select specific java for this tool
IF "x%RELEASATOR_JAVA%x" == "xx" SET RELEASATOR_JAVA=%JAVA_HOME%
IF EXIST "%RELEASATOR_JAVA%\bin\java.exe" GOTO :WeHaveJava

echo ERROR: No java configured. Please ensure that either RELEASATOR_JAVA or JAVA_HOME point to a JAVA installation (version 5 or higher) >&2
echo nojava
exit /B -1
GOTO :End

:WeHaveJava

REM JVM options can be added to enable more settings, for instance to setup debugging
SET RELEASATOR_JVM_OPTIONS=-ea -Xmx1G %RELEASATOR_JVM_OPTIONS%

SET RELEASATOR_HOME=..

"%RELEASATOR_JAVA%\bin\java.exe" %RELEASATOR_JVM_OPTIONS% -jar "%RELEASATOR_HOME%"\releasator-%RELEASATOR_VERSION%.jar %*
:End
