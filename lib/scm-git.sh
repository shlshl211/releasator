#
# Implementation of the GIT SCM functions.
# @author Petr Kozelka
#

SCM="GIT"

function GIT_parseInfo() {
    # TODO gather USER_EMAIL, USER_FULLNAME, SCM_URLs
    USER_EMAIL=$(git config user.email)
    USER_FULLNAME=$(git config user.name)
    echo "AUTHOR=$USER_FULLNAME <$USER_EMAIL>"
    [ -z "$USER_FULLNAME" ] && return 1
    [ -z "$USER_EMAIL" ] && return 1
    return 0
}

function GIT_commit() {
    local message="$1"

    echo "AUTHOR=$USER_FULLNAME <$USER_EMAIL>" >&2
    git commit -am "$message" --author="$USER_FULLNAME <$USER_EMAIL>" >&2
    git rev-parse HEAD
}

function GIT_revertCommit() {
    local revision="$1"
    git revert -n --no-edit "$revision" || return 1
}

function GIT_tag() {
    local tag="$1"
    local message="$2"
    git tag "$tag" -m "$message"
}
