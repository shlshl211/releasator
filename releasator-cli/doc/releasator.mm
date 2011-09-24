<map version="0.7.1">
<node ID="_Freemind_Link_1932490549" TEXT="RELEASATOR">
<node ID="_Freemind_Link_1055347097" TEXT="upload-release" POSITION="left">
<node ID="_Freemind_Link_1423214804" TEXT="scm checkout"/>
<node ID="_Freemind_Link_1526570401" TEXT="build"/>
<node ID="_Freemind_Link_1509782225" TEXT="extend changes.xml"/>
<node ID="_Freemind_Link_1917594777" TEXT="attach changes.xml"/>
<node ID="_Freemind_Link_214604493" TEXT="upload"/>
</node>
<node ID="_Freemind_Link_487926510" TEXT="upload-snapshot" POSITION="left"/>
<node ID="Freemind_Link_1256017696" TEXT="input" FOLDED="true" POSITION="right">
<node ID="Freemind_Link_1254756181" TEXT="sourceLocation">
<font NAME="SansSerif" BOLD="true" SIZE="12"/>
<node ID="Freemind_Link_1793009501" TEXT="scm url"/>
<node ID="Freemind_Link_769618466" TEXT="vcs id"/>
</node>
<node ID="Freemind_Link_1778635052" TEXT="releaseVersion">
<font NAME="SansSerif" BOLD="true" SIZE="12"/>
</node>
<node ID="Freemind_Link_1270599844" TEXT="nextSnapshotVersion"/>
<node ID="Freemind_Link_1465664808" TEXT="codename">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="Freemind_Link_1678387706" TEXT="author">
<node ID="Freemind_Link_1720702423" TEXT="fullname"/>
<node ID="Freemind_Link_562756780" TEXT="email"/>
<node ID="Freemind_Link_1062458351" TEXT="signing client certificate"/>
<node ID="Freemind_Link_1274568020" TEXT="vcs login">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
</node>
<node ID="Freemind_Link_101691647" TEXT="repository">
<node TEXT="&quot;allrepos&quot; url to prevent id reuse"/>
<node TEXT="&quot;targetLoadUrl&quot; to check right after upload"/>
<node TEXT="&quot;uploadUrl&quot; to use for uploading"/>
</node>
</node>
<node ID="_Freemind_Link_872748494" TEXT="mark" POSITION="right">
<edge WIDTH="thin"/>
<font NAME="SansSerif" SIZE="12"/>
<node TEXT="vcs.checkout(url,basedir)"/>
<node ID="_Freemind_Link_1707461494" COLOR="#ff0033" TEXT="vcs.lock(basedir)"/>
<node ID="_Freemind_Link_884414376" TEXT="verify" FOLDED="true">
<node ID="_Freemind_Link_526775772" TEXT="multipom: version consistency"/>
<node ID="_Freemind_Link_586580679" TEXT="pom/changes id consistency"/>
<node ID="_Freemind_Link_723389177" TEXT="version/releaseVersion consistency"/>
<node ID="_Freemind_Link_1156039699" TEXT="version collission">
<node ID="_Freemind_Link_163688003" TEXT="in changes.xml"/>
<node ID="_Freemind_Link_306459671" TEXT="in source repository (tag)"/>
<node ID="_Freemind_Link_946974383" TEXT="in artifact repositories"/>
</node>
<node ID="_Freemind_Link_1425764563" TEXT="custom policies to detect debug configuration in poms"/>
</node>
<node ID="_Freemind_Link_457204619" TEXT="modify-for-tag" FOLDED="true">
<font NAME="SansSerif" BOLD="true" SIZE="12"/>
<node ID="_Freemind_Link_116740153" TEXT="changes.xml">
<node ID="_Freemind_Link_1318442046" TEXT="reindent"/>
<node ID="_Freemind_Link_871779781" TEXT="releaseVersion -&gt; version"/>
<node ID="_Freemind_Link_1458582590" TEXT="unreleased... -&gt; released">
<node ID="_Freemind_Link_1704013816" TEXT="time"/>
<node ID="_Freemind_Link_330783695" TEXT="author"/>
<node ID="_Freemind_Link_1287138645" TEXT="vcs id+path+tagpath+revision"/>
<node ID="_Freemind_Link_756646533" TEXT="tool stack">
<node ID="_Freemind_Link_1888439057" TEXT="releasator"/>
<node ID="_Freemind_Link_1297149517" TEXT="maven">
<edge WIDTH="thin"/>
</node>
<node ID="_Freemind_Link_1200270499" TEXT="java"/>
<node ID="_Freemind_Link_31988126" TEXT="os"/>
<node ID="_Freemind_Link_1138556611" TEXT="cpu"/>
</node>
</node>
<node ID="_Freemind_Link_304667827" TEXT="read publicArtifactId">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="_Freemind_Link_1934778313" TEXT="add working copy signature">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="_Freemind_Link_357646599" TEXT="list policy errors/warnings"/>
</node>
<node ID="_Freemind_Link_154946048" TEXT="pom.xml">
<node ID="_Freemind_Link_1069678073" TEXT="read tempArtifactId">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="_Freemind_Link_1691571858" TEXT="publicArtifactId -&gt; artifactId">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="_Freemind_Link_1806376106" TEXT="releaseVersion -&gt; version"/>
<node ID="_Freemind_Link_858743469" TEXT="add scm"/>
</node>
<node TEXT="hook: pre-release-permanent-changes">
<node ID="_Freemind_Link_1908564932" TEXT="**/*">
<node ID="_Freemind_Link_245064752" TEXT="time + version -&gt; @since -"/>
<node TEXT="detect TODOs, fail"/>
</node>
</node>
</node>
<node ID="_Freemind_Link_374861515" TEXT="build (for verification)" FOLDED="true">
<node ID="_Freemind_Link_1803755988" TEXT="on clean checkout"/>
<node ID="_Freemind_Link_1148437489" TEXT="against empty repository"/>
</node>
<node ID="_Freemind_Link_459559907" TEXT="modify-for-trunk (preparations)" FOLDED="true">
<font NAME="SansSerif" BOLD="true" SIZE="12"/>
<node ID="_Freemind_Link_1251003719" TEXT="changes.xml">
<node ID="_Freemind_Link_828563667" TEXT="insert &lt;unreleased&gt;"/>
</node>
<node ID="_Freemind_Link_670932371" TEXT="pom.xml">
<node ID="_Freemind_Link_396283215" TEXT="remove scm"/>
<node ID="_Freemind_Link_75111211" TEXT="tempArtifactId -&gt; artifactId">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="_Freemind_Link_1289877598" TEXT="nextSnapshotVersion -&gt; version"/>
</node>
</node>
<node ID="_Freemind_Link_572158252" TEXT="vcs.commit(&apos;release g:a:v&apos;)&apos;"/>
<node ID="_Freemind_Link_1974800683" TEXT="vcs.tag(...)"/>
<node TEXT="use prepared modifications">
<font ITALIC="true" NAME="SansSerif" SIZE="12"/>
</node>
<node ID="_Freemind_Link_1204140712" TEXT="vcs.commit(&apos;preparing for development after release g:a:v&apos;)"/>
<node ID="_Freemind_Link_498357165" COLOR="#ff3300" TEXT="vcs.unlock(basedir)"/>
</node>
<node TEXT="upload" POSITION="right">
<node TEXT="vcs.checkout(release_tag)"/>
<node TEXT="build"/>
<node TEXT="send"/>
</node>
<node ID="Freemind_Link_1856158303" TEXT="output" POSITION="right">
<node ID="Freemind_Link_1897796290" TEXT="tag">
<font NAME="SansSerif" BOLD="true" SIZE="12"/>
</node>
<node ID="Freemind_Link_1797830535" TEXT="list of errors/warnings"/>
<node ID="Freemind_Link_1388434719" TEXT="build log"/>
<node ID="Freemind_Link_814119542" TEXT="repository listing"/>
</node>
</node>
</map>
