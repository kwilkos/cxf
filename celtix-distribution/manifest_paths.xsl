<?xml version="1.0"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <!-- define a lastIndexOf named template -->
    <xsl:template name="lastIndexOf">
        <!-- declare that it takes two parameters - the string and the char -->
        <xsl:param name="string" />
        <xsl:param name="char" />
        <xsl:choose>
            <xsl:when test="contains($string, $char)">

                <xsl:call-template name="lastIndexOf">
                    <xsl:with-param name="string" select="substring-after($string, $char)" />
                    <xsl:with-param name="char" select="$char" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="/project/path/fileset">
        <xsl:text>Class-Path:&#32;</xsl:text>
        <xsl:apply-templates select="include"/>
    </xsl:template>
    <xsl:template match="include">
        <xsl:call-template name="lastIndexOf">
            <xsl:with-param name="string" select="@name" />
            <xsl:with-param name="char" select="'/'" />
        </xsl:call-template>
        <xsl:text>&#32;</xsl:text>
    </xsl:template>
    <xsl:template match="echo"/>
</xsl:stylesheet>
