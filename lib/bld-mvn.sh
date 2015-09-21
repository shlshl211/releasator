#
# Implements the Maven buildsystem
# @author Petr Kozelka
#

BLD="MVN"

function MVN_parseInfo() {
    #TODO gather groupId, artifactId, develVersion
    ORIG_BUILDNUMBER=$(xmllint --xpath '/*/*[name()="properties"]/*[name()="buildNumber"]/text()' pom.xml)
  	NAME=$(xmllint --xpath '/*/*[name()="artifactId"]/text()' pom.xml)
  	DEVEL_VERSION=$(xmllint --xpath '/*/*[name()="version"]/text()' pom.xml)
    #TODO check that values are not null, develv is snapshot, etc.
    return 0
}

function MVN_download() {
    mvn dependency:go-offline
    find
}

function MVN_build() {
    mvn deploy -DskipTests\
      -Duser.name="${USER_FULLNAME}"\
      -DaltDeploymentRepository="fs::default::file://$TMP/output"
}

function MVN_setVersion() {
    local releaseVersion="$1"

    # mvn versions:set -DnewVersion="$newVersion"
    echo "Switching version '$DEVEL_VERSION' to '$releaseVersion'" >&2
    [ -n "$releaseVersion" ] || return 1
    find * -name "pom.xml" | grep -v 'target/' | grep -v 'src/it/' >$TMP/poms
    cat "$TMP/poms" | xargs sed -i 's:<version>'"$DEVEL_VERSION"'</version>:<version>'"$releaseVersion"'</version>:g;'
    cat "$TMP/poms" | xargs grep '\-SNAPSHOT' && return 1 >&2
    echo "SNAPSHOT versions replaced." >&2
}
