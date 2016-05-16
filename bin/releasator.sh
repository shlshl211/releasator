#!/bin/bash
#
# Releasator (C) Petr Kozelka
#
# - uses Maven Release Plugin to tag and upload new release.
#

##
#CMD#cancel : Cancels the prepared release.
# It does NOT affect remote systems (GIT, Nexus)
#
function CMD_cancel() {
    local scmTag=$(cat "$TMP/tagName")
    if [ -s "release.properties" ]; then
        # delete release tag
        [ -z "$scmTag" ] && scmTag=$(sed -n '/^scm\.tag=/{s:^[^=]*=::;p;}' release.properties)
        rm -v release.properties
        echo "removing files pom.xml.releaseBackup"
        find * -name pom.xml.releaseBackup | xargs rm -v
    else
        echo "WARNING: file release.properties not found" >&2
    fi
    git tag -d ${scmTag}
    if [ -s "$TMP/cancel-hash" ]; then
        local cancelHash=$(cat "$TMP/cancel-hash")
        echo "Resetting back to $cancelHash"
        git reset --hard ${cancelHash} && rm -v "$TMP/cancel-hash"
    else
        echo "ERROR: file $TMP/cancel-hash not found, cannot drop release commits" >&2
    fi
    rm -v "$TMP/settings.xml" "$TMP/scm.url"
    rmdir -v "$TMP" || echo "ERROR: could not remove directory '$TMP', please do it manually"
    git status --porcelain
}

#CMD#close : Closes the release.
##
# Now it just pushes into remote git; in future, it should also upload to Nexus (which should be removed from PREPARE)
#
function CMD_close() {
    git push || return 1
    git push --tags || return 1
    rm -rf "$TMP"
}

##
#CMD#xupload : experimental upload
#
function CMD_xupload() {
    NEXUS_URL=$(cat ~/.m2/nexus.url)
    [ -n "$NEXUS_URL" ] || exit 1
    echo "NEXUS_URL=$NEXUS_URL"
    echo "1: $TMP/output"
    listModulesInLocalRepo "$TMP/output"
    echo "2:"
    nexusUploadByFiles_parse
    echo "3:"
    mvnDeployByFiles
}

##
#CMD#pre : v2 prepare
# @param releaseVersion
#
function CMD_pre() {
    v2_pre "$@"
}

##
#CMD#pub : v2 publish
#
function CMD_pub() {
    v2_pub "$@"
}

#### MAIN ####
D=$(readlink -f $0)
D=${D%/bin/*}

source $D/lib/bld-mvn.sh
source $D/lib/chg-mvn.sh
source $D/lib/scm-git.sh
source $D/lib/publish-mdeploy.sh
source $D/lib/rls-apis.sh
source $D/lib/rls-v2.sh
source $D/lib/rls-main.sh
