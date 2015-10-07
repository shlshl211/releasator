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

function phase() {
    local phaseName=$1
    echo "--- PHASE: '$phaseName' ---"
    # TODO call extensions bound to this phase
}

function v2_pre() {
    local releaseVersion="$1"

# todo basic validations
# todo gpg-signing
# todo changes.xml (or Changelog, Changelog.md, README.md) support
# todo allow auto-generated changes
# todo support changing buildNumber, scmRevision, ...
#
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
    dbgrun SCM_commit "[releasator] Preparing version $releaseVersion" >$TMP/preparing.hash || return 1
    local hash=$(cat $TMP/preparing.hash)
    printf "Pre-release revision: '%s'\n" "$hash"
    phase TAG || return 1
    dbgrun SCM_tag "Released by releasator" || return 1
    phase UNEDIT || return 1
    dbgrun SCM_revertCommit "$hash" || return 1
    dbgrun CHG_postRelease || return 1
    dbgrun SCM_commit "[releasator] Preparing for development after release $releaseVersion" || return 1
    phase PREPARED
    echo "Release $releaseVersion : SUCCESS"
}

function v2_pub() {
    PUBLISHER_upload "$@"
}

function v2_sign() {
    local repo="$TMP/output"
    find $repo -type f -printf "%P\n" | while read uri; do
        local f="$repo/$uri"
        case "${f}" in
        *.md5|*.sha1) continue;;
        *.asc) rm "$f"; continue;;
        esac
        gpg -ab "$f"
    done
}
