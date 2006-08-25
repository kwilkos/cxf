<!--
     Stylesheet to convert schema into java file for test implementation.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:itst="http://tests.iona.com/ittests">

    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="itst:builtIn" mode="javaHolderType">
        <xsl:text>&#10;            Holder&#x3c;</xsl:text>
        <xsl:value-of select="@javaHolder"/>
        <xsl:text>&#x3e;</xsl:text>
    </xsl:template>
    
    <xsl:template match="itst:builtIn" mode="javaType">
        <xsl:value-of select="@javaType"/>
    </xsl:template>

    <xsl:template match="xsd:simpleType|xsd:complexType|xsd:element" mode="javaHolderType">
        <xsl:text>&#10;            Holder&#x3c;</xsl:text>
        <xsl:apply-templates select="." mode="javaType"/>
        <xsl:text>&#x3e;</xsl:text>
    </xsl:template>
    
    <xsl:template match="xsd:simpleType" mode="javaType">
        <xsl:if test="contains(@name, 'Union')">
            <xsl:value-of select="'String'"/>
        </xsl:if>
        <xsl:if test="not(contains(@name, 'Union'))">
            <xsl:value-of select="@name"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="xsd:simpleType/union//union" mode="javaType">
        <xsl:value-of select="'Fook Yah'"/>
    </xsl:template>

    <xsl:template match="xsd:complexType|xsd:element" mode="javaType">
        <xsl:value-of select="@name"/>
    </xsl:template>

</xsl:stylesheet>
