#
# Implements changelog support ala maven-changes-plugin
#
# - first entry is supposed to represent snapshot; is turned into release version before tagging
# - after tagging, an empty entry is added at the top, to be ready for updates by the developer
#


CHG="CHGMVN"

function CHGMVN_toRelease() {
    local releaseVersion="$1"
    if [ -f "changes.xml" ]; then
        local releaseLine='<release version="'$DEVEL_VERSION'">'
        echo "R=*$releaseLine*"
        local releaseLineCnt=$(grep "${releaseLine}" changes.xml | wc -l)
        if [ "$releaseLineCnt" != "1" ]; then
            echo "ERROR: changes.xml: expected one release line ($releaseLine) but found $releaseLineCnt" >&2
            return 1
        fi
        local TODAY=$(date '+%F')
        sed 's:'"$releaseLine"':<release version="'$releaseVersion'" date="'"$TODAY"'">:' changes.xml >release-changes.xml || return 1
        sed 's:<body>:<body>\n        <release version="'"$DEVEL_VERSION"'">\n            <!-- add changes here -->\n        </release>:' release-changes.xml >$TMP/next-changes.xml || return 1
        mv "release-changes.xml" "changes.xml"
    fi
}

function CHGMVN_postRelease() {
    if [ -f "changes.xml" ]; then
        echo "Preparing changes.xml for further development"
        mv "$TMP/next-changes.xml" "changes.xml" || return 1
    fi
}
