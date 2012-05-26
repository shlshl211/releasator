<?xml version="1.0" encoding="UTF-8"?>
<!--
Petr Kozelka (C) 2007
pkozelka@gmail.com

Removes element "scm" from the POM.
If parameter "scm" is specified, creates "scm" element at the end of POM file.

Parameter "name-action" can influence how pom's name is altered:
- value "static" sets its value to expansion of "${project.artifactId}:${project.version}"
- value "dynamic" sets its value to "${project.artifactId}:${project.version}" literally
- other values do not change it
-->
<xsl:stylesheet version="1.0"
    xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:pom="http://maven.apache.org/POM/4.0.0"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    extension-element-prefixes="xalan"
    exclude-result-prefixes="pom"
    >
  <xsl:output method="xml" indent="yes" xalan:indent-amount="4" encoding="UTF-8"/>
  <xsl:param name="scm"/>
  <xsl:param name="release-root"/>
  <xsl:param name="url"/>
  <xsl:param name="newArtifactId" select="/*/pom:artifactId"/>
  <xsl:param name="name-action"/>
  <xsl:param name="version">
    <xsl:value-of select="/*/pom:version"/>
    <xsl:if test="not(/*/pom:version)">
      <xsl:value-of select="/*/pom:parent/pom:version"/>
    </xsl:if>
  </xsl:param>

  <xsl:template match="text()[normalize-space()='']"/>
  <xsl:template match="processing-instruction('release-root')"/>
  <xsl:template match="processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    <xsl:text xml:space="preserve">
</xsl:text>
  </xsl:template>
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <!-- -->
  <xsl:template match="pom:project/pom:artifactId/text()">
    <xsl:choose>
      <xsl:when test="$newArtifactId != .">
        <xsl:value-of select="$newArtifactId"/>
        <xsl:message>
          <xsl:text>WARNING: changing artifactId from "</xsl:text>
          <xsl:value-of select="."/>
          <xsl:text>" to "</xsl:text>
          <xsl:value-of select="$newArtifactId"/>
          <xsl:text>"</xsl:text>
        </xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="pom:project/pom:name/text()">
    <xsl:choose>
      <xsl:when test="$name-action='static'">
        <xsl:message>
          <xsl:text>WARNING: static name "</xsl:text>
          <xsl:value-of select="$newArtifactId"/>
          <xsl:text>"</xsl:text>
        </xsl:message>
        <xsl:value-of select="$newArtifactId"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="$version"/>
      </xsl:when>
      <xsl:when test="$name-action='dynamic'">
        <xsl:text>${project.artifactId}:${project.version}</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- -->
  <xsl:template match="pom:project">
    <xsl:if test="$url">
      <xsl:if test="not(pom:url)">
        <xsl:message terminate="yes">ERROR: Element '/project/url' does not exist, cannot modify it</xsl:message>
      </xsl:if>
    </xsl:if>
    <xsl:if test="$release-root">
        <xsl:processing-instruction name="release-root">
            <xsl:value-of select="$release-root"/>
        </xsl:processing-instruction>
    </xsl:if>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:if test="$scm">
        <scm>
          <connection>
            <xsl:value-of select="$scm"/>
          </connection>
          <developerConnection>
            <xsl:value-of select="$scm"/>
          </developerConnection>
          <url>
            <xsl:value-of select="substring-after($scm,'scm:svn:')"/>
          </url>
        </scm>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="/pom:project/pom:scm"/>
  <xsl:template match="/pom:project/pom:url">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
        <xsl:when test="$url">
          <xsl:value-of select="$url"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="node()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
