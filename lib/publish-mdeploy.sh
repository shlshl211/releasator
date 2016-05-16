#
# Publish build results using Maven's deploy:file mojo
# @author Petr Kozelka
#

PUBLISHER="MDF"

function MDF_upload() {
    local publishUrl="$1"
    eval $(cat "$TMP/conf/releasator.conf")
    mvnDeployByFiles "$RELEASATOR_UPLOAD_URL"
}

# -- TODO -- following requires significant polishing --

# TODO: now we should upload the zip as described here: https://support.sonatype.com/entries/22189106-How-can-I-programatically-upload-an-artifact-into-Nexus-
# curl --upload-file my.zip -u admin:admin123 -v http://localhost:8081/nexus/service/local/repositories/releases/content-compressed/foo/bar

function nexusUploadPacked() {
    pushd "$TMP/output"
    zip ../release.zip -r *
    popd
    curl --upload-file .releasator/mss-0.0.900.zip -u deployment:deployment123 -v ${NEXUS_URL?'Nexus url!'}/service/local/repositories/releases/content-compressed
}

function doCmd() {
    echo "$@"
    "$@"
}

# curl -v -F r=releases -F hasPom=true -F e=jar -F file=@pom.xml -F file=@project-1.0.jar -u deployment:deployment123 ${NEXUS_URL?'Nexus url!'}/service/local/artifact/maven/content
function nexusUploadByFiles() {
    nexusUploadByFiles_parse | sort | while read; do
echo "$REPLY"
#        doCmd curl -v -F r=releases -F hasPom=true $REPLY -u deployment:deployment123 ${NEXUS_URL?'Nexus url!'}/service/local/artifact/maven/content || return 1
    done
}

function mvnDeployByFiles() {
    local repoUrl="${1?'Nexus url!'}"
    local repo="$TMP/output"
    cd "$repo"
    local uploadCmd="mvn "
    uploadCmd="$uploadCmd org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy-file"
    uploadCmd="$uploadCmd -DrepositoryId=releasator.repo"
    uploadCmd="$uploadCmd -Durl=$repoUrl"
    uploadCmd="$uploadCmd -s $TMP/upload-settings.xml"
    nexusUploadByFiles_parse | while read groupDir version artifactId ext classifier; do
        echo
        echo "$artifactId:$version:$ext:$classifier"
        case "$classifier" in
            '-'|'') c="";;
            *) c="-$classifier";;
        esac
        local cmd="$uploadCmd"
        if [ "$ext" == "pom" -a "$classifier" == "-" ]; then
            # standalone pom
            cmd="$cmd -Dfile=$groupDir/$artifactId/$version/$artifactId-$version.pom -Dpackaging=pom"
        else
            # artifact + pom
            cmd="$cmd -DpomFile=$groupDir/$artifactId/$version/$artifactId-$version.pom"
            cmd="$cmd -Dfile=$groupDir/$artifactId/$version/$artifactId-$version$c.$ext"
            cmd="$cmd -Dpackaging=$ext"
            [ -n "$classifier" -a "$classifier" != "-" ] && cmd="$cmd -Dclassifier=$classifier"
        fi
        doCmd $cmd || return 1
    done
}

function listModulesInLocalRepo() {
    local repo=${1}
    find $repo -type d -links 2 -printf "%P\n" | while read vdir; do
        local version="${vdir/#*\//}"
        local adir="${vdir%/*}"
        local artifactId="${adir/#*\//}"
        local gdir="${adir%/*}"
        local groupId=${gdir//\//.}
        [ -s "$repo/$vdir/$artifactId-$version.pom" ] || continue
        echo "$vdir $groupId $artifactId $version"
    done
}

function nexusUploadByFiles_parse() {
    local repo="$TMP/output"
    listModulesInLocalRepo "$repo" | while read vdir groupId artifactId version; do
        local gdir=${groupId//\.//}
        pushd "$repo/$vdir" >/dev/null
        local prefix="$artifactId-$version"
        local standalonePom="true"
        for f in $(ls -1 ${prefix}.* ${prefix}-* 2>/dev/null); do
            local ext=${f/#*\./}
            case "$ext" in
            md5|sha1|pom) continue;;
            asc) # this is always a second extension
                local ff=${f%.asc}
                ext="${ff/#*\./}.asc"
                ;;
            esac
            case "$f" in
            *'.tar.gz') ext="tar.gz";;
            esac
            standalonePom="false"
            local result="$gdir $version $artifactId $ext"
            # g=$groupId a=$artifactId v=$version
            local c="-"
            case "$f" in
            "$prefix-"*"$ext")
                c=${f:${#prefix}+1}
                c=${c%.$ext}
                ;;
            esac
            echo "$result $c"
        done
        $standalonePom && echo "$gdir $version $artifactId pom"
        popd >/dev/null
    done
}
