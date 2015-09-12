#
# Delegates API calls to the implementations
# @author Petr Kozelka
#

#######################
# SCM api
#######################

##
# Gather and validate initial data
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
  ${SCM}_commit "$@"
}

##
# Revert specified commit, by adding new commit with specified message
# @param revision
# @param message
#
function SCM_revertCommit() {
  ${SCM}_revertCommit "$@"
}

##
# Tag current state
# @param tag the tag name, usually in the form 'artifactId-version'
# @param message  tag object/commit message if supported by scm
#
function SCM_tag() {
  ${SCM}_tag "$@"
}


#######################
# Buildsystem API
#######################

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
# Publisher API
#######################
#TODO: support multiple publishers

##
# Uploads release to a public location.
#
function PUBLISHER_upload() {
  ${PUBLISHER}_upload
}
