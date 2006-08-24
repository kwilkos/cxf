<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:param name="checkstyleconfig"/>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="checkstyle-configurations">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>


            <xsl:choose>
                <xsl:when test="not(check-configuration/@name='CXF Checks')">
                    <check-configuration name="CXF Checks" type="external" description="">
                        <xsl:attribute name="location"><xsl:value-of select="$checkstyleconfig"/></xsl:attribute>
                    </check-configuration>
                </xsl:when>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

