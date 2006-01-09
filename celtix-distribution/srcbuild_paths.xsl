<?xml version="1.0"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt">

    <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>
    <xsl:strip-space elements="*"/>
    

    <!-- copy attributes from any node -->
    <xsl:template match="@*" mode="attribute_copy">
        <xsl:attribute name="{name(.)}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="path">
        <project name="source-build-paths">
            <property>
                <xsl:attribute name="file">${user.home}/.m2/maven.properties</xsl:attribute>
            </property>
            <property name="maven.repo.local">
                <xsl:attribute name="value">${user.home}/.m2/repository</xsl:attribute>
            </property>
            <path id="srcbuild.classpath.path">
                <xsl:copy-of select="*"/>
            </path>
            <property name="srcbuild.classpath" refid="srcbuild.classpath.path"/>
        </project>
    </xsl:template>

    <xsl:template match="echo">
    </xsl:template>

</xsl:stylesheet>
