#
# Implements the Maven buildsystem
# @author Petr Kozelka
#

BLD="MVN"

function MVN_download() {
  mvn dependency:go-offline
}

function MVN_build() {
  mvn -o deploy
}
