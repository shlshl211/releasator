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
    dbgrun BLD_setVersion "$releaseVersion" || return 1
    dbgrun BLD_download || return 1
    dbgrun BLD_build || return 1
    dbgrun SCM_commit "[releasator] Preparing version $releaseVersion" >$TMP/preparing.hash || return 1
    dbgrun SCM_tag "$artifactId-$releaseVersion" "Released by releasator" || return 1
    dbgrun local hash=${TMP/preparing.hash}
    dbgrun SCM_revertCommit "[releasator] Preparing for development after release $releaseVersion" || return 1
    echo "Release $releaseVersion : SUCCESS"
}

function v2_pub() {
    PUBLISHER_upload
}
