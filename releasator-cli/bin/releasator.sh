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
RELEASATOR_HOME="${F%/*/*}"

# Variable RELEASATOR_JAVA: you may wish to select specific java for this tool
[ -z "${RELEASATOR_JAVA}" ] && RELEASATOR_JAVA=${JAVA_HOME}
if ! [ -x "$RELEASATOR_JAVA/bin/java" ]; then
	echo "ERROR: No java configured. Please ensure that either RELEASATOR_JAVA or JAVA_HOME point to a JAVA installation (version 5 or higher)" >&2
	exit -1
fi

[ -z "${RELEASATOR_JVM_OPTIONS}" ] && RELEASATOR_JVM_OPTIONS="-ea -Xmx1G ${RELEASATOR_JVM_OPTIONS}"
exec $RELEASATOR_JAVA/bin/java $RELEASATOR_JVM_OPTIONS -jar $RELEASATOR_HOME/releasator.jar $*
