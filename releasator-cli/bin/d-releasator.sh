#!/bin/bash
# %MACRO:LocateSelf:begin%
D=${0%/*}
F=$( find $0 -printf %l )
if [ -z "$F" ]; then
	# it is not symlink
	case "$0" in
	/*) F=$0;;
	*) F=$PWD/$0
	esac
else
	# it is symlink
	case "$F" in
	/*);;
	*) F="$D/$F";;
	esac
fi
# now F=absolute path here
F=${F//\/\.\///}
# %MACRO:LocateSelf:end%
D="${F%/*/*}"

# this is to properly locate primary artifact
BUILDBOX_RELEASATOR_VERSION="1-SNAPSHOT" ##REPLACEWITH-INTERPOLATION:BUILDBOX_RELEASATOR_VERSION="${project.version}"

echo -e "DEVELOPMENT MODE - use following to setup production mode:\n mvn net.sf.buildbox.maven:installer:1-SNAPSHOT:install -Dwhat=net.sf.buildbox:releasator:${BUILDBOX_RELEASATOR_VERSION}" >&2 ##REPLACEWITH:

# Variable BUILDBOX_HOME: you can redefine the default of '~/.buildbox'
[ -z "${BUILDBOX_HOME}" ] && BUILDBOX_HOME=$D

# Variable BUILDBOX_REPO: points to the repository for launching tools
[ -z "${BUILDBOX_REPO}" ] && BUILDBOX_REPO=${M2_REPO:-$HOME/.m2/repository} ##REPLACEWITH:[ -z "${BUILDBOX_REPO}" ] && BUILDBOX_REPO=${BUILDBOX_HOME}/maven-repo

# Variable BUILDBOX_RELEASATOR_JAVA: you may wish to select specific java for this tool
[ -z "${BUILDBOX_RELEASATOR_JAVA}" ] && BUILDBOX_RELEASATOR_JAVA=${JAVA_HOME}
if ! [ -x "$BUILDBOX_RELEASATOR_JAVA/bin/java" ]; then
	echo "ERROR: No java configured. Please ensure that either BUILDBOX_RELEASATOR_JAVA or JAVA_HOME point to a JAVA installation (version 5 or higher)" >&2
	exit -1
fi

# Variable BUILDBOX_RELEASATOR_JVM_OPTIONS: enables more settings, for instance to setup debugging
BUILDBOX_RELEASATOR_JVM_OPTIONS="-ea -Xmx1G ${BUILDBOX_RELEASATOR_JVM_OPTIONS}"
exec $BUILDBOX_RELEASATOR_JAVA/bin/java $BUILDBOX_RELEASATOR_JVM_OPTIONS -jar $BUILDBOX_REPO/net/sf/buildbox/releasator/${BUILDBOX_RELEASATOR_VERSION}/releasator-${BUILDBOX_RELEASATOR_VERSION}.jar $*
