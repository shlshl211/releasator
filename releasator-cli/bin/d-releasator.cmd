@echo off
setlocal enableextensions enabledelayedexpansion

REM this is to properly locate primary artifact
SET BUILDBOX_RELEASATOR_VERSION=1-SNAPSHOT&REM ##REPLACEWITH-INTERPOLATION:SET BUILDBOX_RELEASATOR_VERSION=${project.version}

echo 'DEVELOPMENT MODE; to setup production mode: mvn net.sf.buildbox.maven:installer:1-SNAPSHOT:install -Dwhat=net.sf.buildbox:releasator:%BUILDBOX_RELEASATOR_VERSION%' >&2&REM ##REPLACEWITH:
REM Variable BUILDBOX_HOME: you can redefine the default of '~/.buildbox'
IF "x%BUILDBOX_HOME%x" == "xx" SET BUILDBOX_HOME=%USERPROFILE%\.buildbox

REM Variable BUILDBOX_REPO: points to the repository for launching tools
IF "x%M2_REPO%x" == "xx" SET M2_REPO=%USERPROFILE%\.m2\repository
IF "x%BUILDBOX_REPO%x" == "xx" SET BUILDBOX_REPO=%M2_REPO%&REM ##REPLACEWITH:IF "x%BUILDBOX_REPO%x" == "xx" SET BUILDBOX_REPO=%BUILDBOX_HOME%/maven-repo

REM Variable BUILDBOX_RELEASATOR_JAVA: you may wish to select specific java for this tool
IF "x%BUILDBOX_RELEASATOR_JAVA%x" == "xx" SET BUILDBOX_RELEASATOR_JAVA=%JAVA_HOME%
IF EXIST "%BUILDBOX_RELEASATOR_JAVA%\bin\java.exe" GOTO :WeHaveJava

echo ERROR: No java configured. Please ensure that either BUILDBOX_RELEASATOR_JAVA or JAVA_HOME point to a JAVA installation (version 5 or higher) >&2
echo nojava
exit /B -1
GOTO :End

:WeHaveJava

REM JVM options can be added to enable more settings, for instance to setup debugging
SET BUILDBOX_RELEASATOR_JVM_OPTIONS=-ea -Xmx1G %BUILDBOX_RELEASATOR_JVM_OPTIONS%

"%BUILDBOX_RELEASATOR_JAVA%\bin\java.exe" %BUILDBOX_RELEASATOR_JVM_OPTIONS% -jar "%BUILDBOX_REPO%"/net/sf/buildbox/releasator/%BUILDBOX_RELEASATOR_VERSION%/releasator-%BUILDBOX_RELEASATOR_VERSION%.jar %*
:End