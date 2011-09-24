<?xml version="1.0" encoding="UTF-8"?>
<!--
	Merges mirrors and servers from current.settings to the template
 -->
<xsl:stylesheet version="1.0"
    xmlns="http://buildbox.sf.net/changes/2.0"
    xmlns:settings="http://maven.apache.org/POM/4.0.0"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    extension-element-prefixes="xalan">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="2"/>

  <xsl:param name="current.settings.xml"/>
  <xsl:param name="tmp.repo.base"/>
  <xsl:variable name="current.settings" select="document($current.settings.xml,/)/*"/>

  <xsl:template match="/settings|/settings:settings">
    <xsl:if test="not($current.settings/mirrors | $current.settings/settings:mirrors)">
      <xsl:message terminate="yes">
        <xsl:text>ERROR: No mirrors found in settings file '</xsl:text>
        <xsl:value-of select="$current.settings.xml"/>
        <xsl:text>'</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="$current.settings/mirrors|$current.settings/settings:mirrors"/>
      <xsl:apply-templates select="$current.settings/servers|$current.settings/settings:servers"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="TMP">
    <xsl:value-of select="$tmp.repo.base"/>
  </xsl:template>

  <xsl:template match="text()[normalize-space(.)='']"/>
  <xsl:template match="settings:*">
    <xsl:element name="{local-name(.)}">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
