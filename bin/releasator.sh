#!/bin/bash
#
# Releasator (C) Petr Kozelka
#
# - uses Maven Release Plugin to tag and upload new release.
# - configured to work with GIT and slightly customized settings
#

# Features
#
# - when pom.xml defines property "buildNumber", the second parameter is required and set to it (just for the release)
# - TODO: when there is changes.xml, with a SNAPSHOT as the first release, it will be closed and new one prepared afterwards
# - TODO: deploy only locally, and publish in release_perform!

##
# Prepares release. That is: change version, commit, tag, change version back, commit. Plus some stuff around this.
# @param #1 - release version
# @param #2 - release codename to replace the "buildNumber" property in the toplevel pom. Mandatory if the toplevel pom has a buildNumber property
#
function prepare() {
	VERSION=${1?'Please specify version as the first argument'}
	ORIG_BUILDNUMBER=$(xmllint --xpath '/*/*[name()="properties"]/*[name()="buildNumber"]/text()' pom.xml)
	NAME=$(xmllint --xpath '/*/*[name()="artifactId"]/text()' pom.xml)
	TAGNAME="$NAME-$VERSION"
	DEVEL_VERSION=$(xmllint --xpath '/*/*[name()="version"]/text()' pom.xml)
	case "$DEVEL_VERSION" in
	*'-SNAPSHOT');;
	*) echo "ERROR: Current version is not a snapshot: $DEVEL_VERSION" >&2; return 1;;
	esac
	SCM_DC=$(xmllint --xpath '/*/*[name()="scm"]/*[name()="developerConnection"]/text()' pom.xml)
	if [ -z "$SCM_DC" ]; then
		echo "ERROR: Missing tag /project/scm/developerConnection" >&2
#		exit 1
	fi
	local existingTags=$(git tag -l "$TAGNAME")
	case "$existingTags" in
	'');;
	*) echo "ERROR: Tag '$TAGNAME' already exists: $existingTags" >&2; return 1;;
	esac
	if [ -f "changes.xml" ]; then
		local releaseLine='<release version="'$DEVEL_VERSION'" '
		echo "R=*$releaseLine*"
		local releaseLineCnt=$(grep "${releaseLine}" changes.xml | wc -l)
		if [ "$releaseLineCnt" != "1" ]; then
			echo "ERROR: changes.xml: expected one release line ($releaseLine) but found $releaseLineCnt" >&2
			return 1
		fi
		local TODAY=$(date '+%F')
		sed 's:'"$releaseLine"':<release version="'$VERSION'" date="'"$TODAY"'" :' changes.xml >release-changes.xml
		mv "release-changes.xml" "changes.xml"

		sed 's:<body>:<body>\n        <release version="'"$DEVEL_VERSION"'">\n        <!-- add changes here -->\n        </release>:' changes.xml >.git/next-changes.xml
#		echo "ERROR: Implementation handling changes.xml is missing" >&2; return 1
	fi
	# store hash of pre-release state, to allow cancellation
	mkdir -p ".releasator"
	git rev-parse HEAD >".releasator/cancel-hash"
	#
	if [ -n "$ORIG_BUILDNUMBER" ]; then
		CODENAME=${2?'This project uses buildNumber property. Please specify codename as the second argument to be used for buildNumber'}
		sed -i 's:<\(buildNumber>\).*</buildNumber>:<\1'"$CODENAME"'</\1:' pom.xml || return 1
#		git commit -am "Releasing $NAME-$VERSION: changed buildNumber to '$CODENAME'" || return 1
	fi
	git commit -am "[releasator] Pre-release changes for $NAME-$VERSION"
	RELEASE_DIR=$HOME/.m2/releases/$NAME-$VERSION

	echo "Releasing project '$NAME' in version '$DEVEL_VERSION' as version '$VERSION' from $SCM_DC"

	mvn release:clean || return 1
	mvn release:prepare -Darguments="-Duser.name='${USER_FULLNAME}'" \
	-DdevelopmentVersion="${DEVEL_VERSION}" \
	-DreleaseVersion=$VERSION \
	-Dtag=$TAGNAME \
	-DaddSchema=true \
	-DupdateDependencies=false \
	-DlocalRepoDirectory=$RELEASE_DIR/repository \
	-Darguments="-DdeployAtEnd=true" \
	-DpreparationGoals="clean deploy" \
	-DpushChanges=false || return 1

	if [ -f "changes.xml" ]; then
		echo "Preparing changes.xml for further development"
		mv ".git/next-changes.xml" "changes.xml" || return 1
		git commit --amend changes.xml --no-edit || return 1
	fi

	if [ -n "$ORIG_BUILDNUMBER" ]; then
		echo "returning original buildNumber value"
		# silently change the buildNumber back - no special commit
		sed -i 's:<\(buildNumber>\).*</buildNumber>:<\1'"$ORIG_BUILDNUMBER"'</\1:' pom.xml || return 1
		git commit --amend pom.xml --no-edit || return 1
	fi
	echo "removing files pom.xml.releaseBackup"
	find * -name pom.xml.releaseBackup | xargs rm
}

function release_cancel() {
	if [ -s "release.properties" ]; then
		# delete release tag
		local scmTag=$(sed -n '/^scm\.tag=/{s:^[^=]*=::;p;}' release.properties)
		git tag -d ${scmTag}
		rm -v release.properties
	else
		echo "ERROR: file release.properties not found" >&2
	fi
	if [ -s ".releasator/cancel-hash" ]; then
		local cancelHash=$(cat ".releasator/cancel-hash")
		echo "Resetting back to $cancelHash"
		git reset --hard ${cancelHash} && rm -v ".releasator/cancel-hash"
	else
		echo "ERROR: file .releasator/cancel-hash not found, cannot drop release commits" >&2
	fi
	rmdir -v ".releasator"
	git status --porcelain
}

function perform() {
	git push || exit 1
	git push --tags || exit 1
	# TODO: the property below should not be needed!
	mvn release:perform -Darguments="-Duser.name='${USER_FULLNAME}' -Dgoals=deploy" || exit 1
}

#### MAIN ####

if [ ! -w pom.xml ]; then
	echo "ERROR: File is missing or not writable: pom.xml" >&2
	exit 1
fi

if [ ! -r .git/config ]; then
	echo "ERROR: Not a git repository" >&2
	exit 1
fi

USER_FULLNAME=$(git config user.name || exit)

cmd=$1
shift

case "$cmd" in
prepare)
	prepare $*
	;;
perform)
	perform $*
	;;
cancel)
	release_cancel $*
	;;
*)	echo "ERROR: Invalid command: $cmd" >&2
	exit 1
	;;
esac
