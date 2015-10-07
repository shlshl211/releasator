#
# Delegates API calls to the implementations
# @author Petr Kozelka
#

#######################
# SCM api
#######################

##
# Gather and validate initial scm data
#
function SCM_parseInfo() {
    ${SCM}_parseInfo "$@"
}

##
# Commit current changes using specified message
# @param message
# @stdout revision of new commit
#
function SCM_commit() {
echo "dbg ... SCMcommit $@" >&2
    ${SCM}_commit "$@"
}

##
# Revert specified commit, by preparing workspace for new reverse commit
# @param revision
#
function SCM_revertCommit() {
    ${SCM}_revertCommit "$@"
}

##
# Tag current state
# @param message  tag object/commit message if supported by scm
#
function SCM_tag() {
    ${SCM}_tag "$@"
}


#######################
# Buildsystem API
#######################

##
# Gather and validate initial build data
#
function BLD_parseInfo() {
    ${BLD}_parseInfo "$@"
}

##
# Preloads all dependencies and plugins needed for the build.
#
function BLD_download() {
    ${BLD}_download "$@"
}

##
# Builds the project in its current state.
#
function BLD_build() {
    ${BLD}_build "$@"
}

##
# Switches current version to the specified one.
# @param newVersion
#
function BLD_setVersion() {
    ${BLD}_setVersion "$@"
}

#######################
# ChangeLog API
#######################

##
# Turns changelog's SNAPSHOT entry into release
# @param releaseVersion
#
function CHG_toRelease() {
    ${CHG}_toRelease "$@"
}

##
# Adds a new SNAPSHOT entry to changelog, for future updates
#
function CHG_postRelease() {
    ${CHG}_postRelease "$@"
}

#######################
# Publisher API
#######################
#TODO: support multiple publishers

##
# Uploads release to a public location.
# @param url  the public location
#
function PUBLISHER_upload() {
    ${PUBLISHER}_upload "$@"
}
