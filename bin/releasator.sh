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


MRP="org.apache.maven.plugins:maven-release-plugin:2.5.2"

function customizeSettingsXml() {
	local sourceSettings=$1
	local targetSettings=$2
	local releaseDir=$3
	if ! grep -q '</profiles>' $sourceSettings; then
		echo "ERROR: there is no </profiles> tag in $sourceSettings" >&2
		return 1
	fi
	echo "INFO: customizing $sourceSettings" >&2
#	sed 's#</profiles>#<profile><id>release-profile</id><properties><releasator.repo.url>file://'$releaseDir'/output</releasator.repo.url></properties></profile>\n</profiles>#;' "$sourceSettings" >"$targetSettings"
	cp "$sourceSettings" "$targetSettings"
}

##
# adds scm information to pom or wherever needed
#
function scmInfoAdd() {
	local SCM_DC=$(xmllint --xpath '/*/*[name()="scm"]/*[name()="developerConnection"]/text()' pom.xml)
	if [ -n "$SCM_DC" ]; then
		# TODO: create the complete scm tag
		echo "$SCM_DC" >$TMP/scm.url
		return 0
	fi
	echo "ERROR: Missing tag /project/scm/developerConnection" >&2
	return 1
}

##
# removes previously added scm information from pom etc.
#
function scmInfoRemove() {
	if [ -f "$TMP/scm-added" ] ; then
		sed -i '/<scm>/,/<scm\/>/d' pom.xml
	fi
}

##
#CMD#prepare : Prepares release. That is: change version, commit, tag, change version back, commit. Plus some stuff around this.
# @param #1 - release version
# @param #2 - release codename to replace the "buildNumber" property in the toplevel pom. Mandatory if the toplevel pom has a buildNumber property
# TODO: get rid of uploading to nexus here - it should happen in upload!
#
function CMD_prepare() {
	VERSION=${1?'Please specify version as the first argument'}

	if [ ! -w pom.xml ]; then
		echo "ERROR: File is missing or not writable: pom.xml" >&2
		exit 1
	fi

	if [ ! -r .git/config ]; then
		echo "ERROR: Not a git repository" >&2
		exit 1
	fi

	USER_FULLNAME=$(git config user.name || exit)
	ORIG_BUILDNUMBER=$(xmllint --xpath '/*/*[name()="properties"]/*[name()="buildNumber"]/text()' pom.xml)
	NAME=$(xmllint --xpath '/*/*[name()="artifactId"]/text()' pom.xml)
	TAGNAME="$NAME-$VERSION"
	DEVEL_VERSION=$(xmllint --xpath '/*/*[name()="version"]/text()' pom.xml)
	mkdir -p "$TMP"
	case "$DEVEL_VERSION" in
	*'-SNAPSHOT');;
	*) echo "ERROR: Current version is not a snapshot: $DEVEL_VERSION" >&2; return 1;;
	esac

	scmInfoAdd || return 1
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
	git rev-parse HEAD >"$TMP/cancel-hash"
	# store settings.xml for use in build
	if [ -s "releasator-settings.xml" ]; then
		customizeSettingsXml "releasator-settings.xml" "$TMP/settings.xml" "$TMP" || return 1
	elif [ -s "$HOME/.m2/releasator-settings.xml" ]; then
		customizeSettingsXml "$HOME/.m2/releasator-settings.xml" "$TMP/settings.xml" "$TMP" || return 1
	else
		customizeSettingsXml "$HOME/.m2/settings.xml" "$TMP/settings.xml" "$TMP" || return 1
	fi
	#
	if [ -n "$ORIG_BUILDNUMBER" ]; then
		CODENAME=${2?'This project uses buildNumber property. Please specify codename as the second argument to be used for buildNumber'}
		sed -i 's:<\(buildNumber>\).*</buildNumber>:<\1'"$CODENAME"'</\1:' pom.xml || return 1
#		git commit -am "Releasing $NAME-$VERSION: changed buildNumber to '$CODENAME'" || return 1
	fi
	git commit -am "[releasator] Pre-release changes for $NAME-$VERSION"

	echo "Releasing project '$NAME' in version '$DEVEL_VERSION' as version '$VERSION' from $(cat $TMP/scm.url)"

	mvn $MRP:clean || return 1
	mvn $MRP:prepare -s "$TMP/settings.xml"\
	-DdevelopmentVersion="${DEVEL_VERSION}" \
	-DreleaseVersion=$VERSION \
	-Dtag=$TAGNAME \
	-DaddSchema=true \
	-DupdateDependencies=false \
	-DlocalRepoDirectory=$TMP/repository \
	-Darguments="-DdeployAtEnd=true" \
	-DpreparationGoals="clean deploy" \
	-Duser.name='${USER_FULLNAME}' \
	-DpushChanges=false || return 1

#	-Darguments="-Duser.name='${USER_FULLNAME} -DskipTests'" \
#	-DskipTests \
#

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

##
#CMD#cancel : Cancels the prepared release.
# It does NOT affect remote systems (GIT, Nexus)
#
function CMD_cancel() {
	if [ -s "release.properties" ]; then
		# delete release tag
		local scmTag=$(sed -n '/^scm\.tag=/{s:^[^=]*=::;p;}' release.properties)
		git tag -d ${scmTag}
		rm -v release.properties
		echo "removing files pom.xml.releaseBackup"
		find * -name pom.xml.releaseBackup | xargs rm -v
	else
		echo "ERROR: file release.properties not found" >&2
	fi
	if [ -s "$TMP/cancel-hash" ]; then
		local cancelHash=$(cat "$TMP/cancel-hash")
		echo "Resetting back to $cancelHash"
		git reset --hard ${cancelHash} && rm -v "$TMP/cancel-hash"
	else
		echo "ERROR: file $TMP/cancel-hash not found, cannot drop release commits" >&2
	fi
	rm -v "$TMP/settings.xml" "$TMP/scm.url"
	rmdir -v "$TMP" || echo "ERROR: could not remove directory '$TMP', please do it manually"
	git status --porcelain
}

#CMD#upload : Publishes the release.
##
# Now it just pushes into remote git; in future, it should also upload to Nexus (which should be removed from PREPARE)
#
function CMD_upload() {
	git push || exit 1
	git push --tags || exit 1
	# TODO: now we should upload the zip as described here: https://support.sonatype.com/entries/22189106-How-can-I-programatically-upload-an-artifact-into-Nexus-
	# curl --upload-file my.zip -u admin:admin123 -v http://localhost:8081/nexus/service/local/repositories/releases/content-compressed/foo/bar
}

#### MAIN ####
D=$(readlink -f $0)
D=${D%/bin/*}

source $D/lib/rls-main.sh
source $D/lib/bld-mvn.sh
source $D/lib/scm-git.sh
source $D/lib/publish-mdeploy.sh
source $D/lib/rls-apis.sh
