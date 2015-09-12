
function v2_pre() {
  local releaseVersion="$1"

# todo basic validations
  SCM_parseInfo || return 1
  BLD_setVersion "$releaseVersion" || return 1
  BLD_download || return 1
  BLD_build || return 1
  SCM_commit "[releasator] Preparing version $releaseVersion" >$TMP/preparing.hash || return 1
  SCM_tag "$artifactId-$releaseVersion" "Released by releasator" || return 1
  local hash=${TMP/preparing.hash}
  SCM_revertCommit "[releasator] Preparing for development after release $releaseVersion" || return 1
  echo "Release $releaseVersion : SUCCESS"
}

function v2_pub() {
  PUBLISHER_upload
}
