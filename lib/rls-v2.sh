DEBUG=true

#TODO add buildNumber support from old implementation of prepare()
#TODO implement extensible lifecycle

function dbgrun() {
    local command=$1
    shift
    $DEBUG && echo ">>> $command $@" >&2
    $command "$@"
    local rv=$?
    $DEBUG && echo "<<< $command returns $rv" >&2
    return $rv
}

function phase() {
    local phaseName=$1
    echo "--- PHASE: '$phaseName' ---"
    # TODO call extensions bound to this phase
}

function notnull() {
    local propName="$1"
    local value=${!propName}
    echo "Validating ${propName}: '${value}'"
    [ -n "${value}" ] && return 0
    echo "ERROR: Property '${propName}' is required but has empty value ('${value}')" >&2
    return 1
}

function build_artifacts() {
    local releaseVersion="$1"
    local config="${2?'Please specify configuration ID as second parameter'}"

    # load configuration
    if [ -d "$TMP" ]; then
        echo "ERROR: Another release is already in progress" >&2
        return 1
    fi
    local configDir
    case "$config" in
    */*) configDir="$config";;
    *) configDir="$HOME/.m2/$config-releasator-conf";;
    esac
    if ! [ -s "$configDir/releasator.conf" ]; then
        echo "ERROR: Missing configuration file: '$configDir/releasator.conf'" >&2
        return 1
    fi
    echo "Validating configuration in '$configDir'"
    eval $(sed '/^#/d' "$configDir/releasator.conf")
    notnull "RELEASATOR_UPLOAD_URL" || return 1
#TODO    notnull "RELEASATOR_VERIFY_URL" || return 1
    notnull "RELEASATOR_DOWNLOAD_URL" || return 1
    mkdir "$TMP"
    cp -aL "$configDir" "$TMP/conf"
    sed -i '/^#/d' "$TMP/conf/releasator.conf"
    if [ -s "$TMP/conf/settings.xml" ]; then
        cp -a "$TMP/conf/settings.xml" "$TMP/upload-settings.xml"
    else
        notnull "RELEASATOR_UPLOAD_USER" || return 1
        notnull "RELEASATOR_UPLOAD_PASSWORD" || return 1
        cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
          <id>releasator.repo</id>
          <username>$RELEASATOR_UPLOAD_USER</username>
          <password>$RELEASATOR_UPLOAD_PASSWORD</password>
        </server>
    /servers>
    <mirrors>
        <mirror>
            <id>RELEASE</id>
            <mirrorOf>*</mirrorOf>
            <name>RELEASE</name>
            <url>$RELEASATOR_DOWNLOAD_URL</url>
        </mirror>
    </mirrors>
  </proxies>
</settings>
EOF
        echo "ERROR: no settings present in $configDir/settings.xml"
    fi
    # in both cases, we derive build settings by removing "servers" part
    sed '/<servers>/,/<\/servers>/d' "$TMP/upload-settings.xml" > "$TMP/build-settings.xml"
# todo gpg-signing
# todo changes.xml (or Changelog, Changelog.md, README.md) support
# todo allow auto-generated changes
# todo support changing buildNumber, scmRevision, ...
#
# todo basic validations
    phase INIT || return 1
    dbgrun BLD_parseInfo "$releaseVersion" || return 1
    dbgrun SCM_parseInfo "$releaseVersion" || return 1

    phase VALIDATE || return 1

    phase PERSISTENT_EDITS || return 1
    dbgrun CHG_toRelease "$releaseVersion" || return 1
    dbgrun BLD_setVersion "$releaseVersion" || return 1

    phase DOWNLOAD || return 1
    dbgrun BLD_download || return 1
    phase TEMPORARY_EDITS || return 1

    phase BUILD || return 1
    dbgrun BLD_build || return 1
    dbgrun SCM_commit "[releasator] Released $NAME-$releaseVersion" >$TMP/preparing.hash || return 1
    local hash=$(cat $TMP/preparing.hash)
    printf "Pre-release revision: '%s'\n" "$hash"

    phase TAG || return 1
    dbgrun SCM_tag "Released by releasator" || return 1

    phase UNEDIT || return 1
    dbgrun SCM_revertCommit "$hash" || return 1
    dbgrun CHG_postRelease || return 1
    dbgrun SCM_commit "[releasator] Preparing for next development after release $releaseVersion" || return 1

    if ${RELEASATOR_PGPSIGN}; then
        phase SIGNING
        pgpsign_output || return 1
    fi

    phase PREPARED
    echo "Successfully prepared release $NAME:$releaseVersion"
}

function upload_artifacts() {
    PUBLISHER_upload "$@"
    #TODO: git push, +tags???
}

function pgpsign_output() {
    local repo="$TMP/output"
    find $repo -type f -printf "%P\n" | while read uri; do
        local f="$repo/$uri"
        case "${f}" in
        *.md5|*.sha1) continue;;
        *.asc) rm "$f"; continue;;
        */maven-metadata.xml) continue;;
        esac
        echo "SIGNING: ${uri}"
        gpg -ab "$f" || return 1
    done
}
