DEBUG=true

function dbgrun() {
    local command=$1
    shift
    $DEBUG && echo ">>> $command $@" >&2
    $command "$@"
    local rv=$?
    $DEBUG && echo "<<< $command returns $rv" >&2
    return $rv
}

function v2_pre() {
    local releaseVersion="$1"

# todo basic validations
    dbgrun SCM_parseInfo || return 1
    dbgrun BLD_parseInfo || return 1
    dbgrun BLD_setVersion "$releaseVersion" || return 1
    dbgrun BLD_download || return 1
    dbgrun BLD_build || return 1
    dbgrun SCM_commit "[releasator] Preparing version $releaseVersion" >$TMP/preparing.hash || return 1
    local hash=$(cat $TMP/preparing.hash)
    printf "Pre-release revision: '%s'\n" "$hash"
    dbgrun SCM_tag "$NAME-$releaseVersion" "Released by releasator" || return 1
    dbgrun SCM_revertCommit "$hash" "[releasator] Preparing for development after release $releaseVersion" || return 1
    echo "Release $releaseVersion : SUCCESS"
}

function v2_pub() {
    PUBLISHER_upload
}
