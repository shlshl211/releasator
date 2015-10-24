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
    mvn dependency:go-offline -l "$TMP/download.log" 2>&1
}

function MVN_build() {
    local MVN_ARGS="-Prelease" #TODO make this parametrized
    local hash=$(cat $TMP/preparing.hash)
    MVN_ARGS="$MVN_ARGS -DscmRevision=$hash"
    MVN_ARGS="$MVN_ARGS -DbuildNumber=RELEASE-$hash"
    mvn deploy "$MVN_ARGS"\
      -Duser.name="${USER_FULLNAME}"\
      -DaltDeploymentRepository="fs::default::file://$TMP/output"\
      | tee "$TMP/build.log" 2>&1 | grep '^.INFO. Building'
}

function MVN_setVersion() {
    local releaseVersion="$1"

    # mvn versions:set -DnewVersion="$newVersion"
    echo "Switching version '$DEVEL_VERSION' to '$releaseVersion'" >&2
    [ -n "$releaseVersion" ] || return 1
    find * -name "pom.xml" | grep -v 'target/' | grep -v 'src/it/' >$TMP/poms
    cat "$TMP/poms" | xargs sed -i 's:<version>'"$DEVEL_VERSION"'</version>:<version>'"$releaseVersion"'</version>:g;'
    cat "$TMP/poms" | xargs grep '[[:alnum:]]\-SNAPSHOT' && return 1 >&2
    echo "SNAPSHOT versions replaced." >&2
}
