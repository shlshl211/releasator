#!/bin/bash
#
# This script setups DEVELOPMENT MODE launcher; it is just a convenience for developers to allow them "hot" news,
# without affecting stable (== installed by installer) version.
# The biggest advantage is that developer needs just to "mvn install" any changed library and it's working; changes to launcher scripts have immediate effect;
# and the only thing he has to pay is, use slightly different names - "dzer" rather than "zer", and "dzcd" rather than "zcd" - as the original keep calling
# stable version working with separate maven repository (~/.buildbox/maven-repo)
#

function __devel_releasator() {
	local cur=${COMP_WORDS[COMP_CWORD]}
#	COMPREPLY=( $( $BUILDBOX_BASEDIR/etc/releasator/xxx.sh -oz "$cur") )
}

BUILDBOX_BASEDIR=$HOME/sf.net/releasator/releasator-cli

#TODO complete -F __devel_releasator

alias d-releasator="$BUILDBOX_BASEDIR/bin/d-releasator.sh"
