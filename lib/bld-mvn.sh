#
# Implements the Maven buildsystem
# @author Petr Kozelka
#

BLD="MVN"

function MVN_parse() {
  #TODO gather groupId, artifactId, develVersion
  return 0
}

function MVN_download() {
  mvn dependency:go-offline
}

function MVN_build() {
  mvn deploy \
    -Duser.name="${USER_FULLNAME}"\
    -DaltDeploymentRepository="fs::default::file://$TMP/output"
}

function MVN_setVersion() {
  local newVersion="$1"

  # mvn versions:set -DnewVersion="$newVersion"
  find * -name "pom.xml" | xargs sed -i 's:<version>$develVersion</version>:<version>$releaseVersion</version>:g;'
}
