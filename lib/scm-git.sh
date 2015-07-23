#
# Implementation of the GIT SCM functions.
# @author Petr Kozelka
#

SCM="GIT"

function GIT_parseInfo() {
  # TODO gather USER_EMAIL, USER_FULLNAME, SCM_URLs
  return 0
}

function GIT_commit() {
  local message="$1"

  git commit -am "$message" --author "$USER_FULLNAME<$USER_EMAIL>"
}

function GIT_revertCommit() {
  local revision="$1"
  local message="$2"
  git revert --no-edit "$revision" || return 1
  git commit --amend -m "$message"
}

function GIT_tag() {
  local tag="$1"
  local message="$2"
  echo TODO
}
