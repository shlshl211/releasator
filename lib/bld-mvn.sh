#
# Implements the Maven buildsystem
# @author Petr Kozelka
#

BLD="MVN"

function MVN_download() {
  mvn dependency:go-offline
}

function MVN_build() {
  mvn -o deploy \
    -Duser.name="${USER_FULLNAME}"\
    -DaltDeploymentRepository="fs::default::file://$TMP/output"
}

function MVN_setVersion() {
  local newVersion="$1"

  mvn versions:set -DnewVersion="$newVersion"
}
