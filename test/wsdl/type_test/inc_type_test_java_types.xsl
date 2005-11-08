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

    <xsl:template match="xsd:element" mode="javaType_suffix">
        <xsl:param name="suffix"/>
        <xsl:apply-templates select="." mode="javaType"/>
        <xsl:if test="string(@type)=''">
            <xsl:value-of select="$suffix"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="xsd:element" mode="javaHolderType_suffix">
        <xsl:param name="suffix"/>
        <xsl:apply-templates select="." mode="javaHolderType"/>
        <xsl:if test="string(@type)=''">
            <xsl:value-of select="$suffix"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="xsd:element[@nillable]" mode="javaHolderType">
        <xsl:apply-templates select="." mode="javaType">
            <xsl:with-param name="isHolder">true</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="xsd:element[@nillable]" mode="javaType">
        <xsl:param name="isHolder"/>
        <xsl:variable name="current-type" select="@type"/>
        <xsl:variable
            name="builtin-javaType"
            select="../../itst:it_test_group/itst:builtIn[@name=$current-type]/@javaType"/>
        <xsl:choose>
            <xsl:when test="$builtin-javaType">
                <xsl:if test="$isHolder='true'">
                    <xsl:text>javax.xml.rpc.holders.</xsl:text>
                </xsl:if>
                <xsl:value-of select="$builtin-javaType"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$isHolder='true'">
                    <xsl:text>com.iona.test.type_test.holders.</xsl:text>
                </xsl:if>
                <xsl:value-of select="substring-after(@type,':')"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$isHolder='true'">
            <xsl:text>Holder</xsl:text>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="itst:builtIn" mode="javaHolderType">
        <xsl:text>&#10;        Holder&#x3c;</xsl:text>
        <xsl:value-of select="@javaHolder"/>
        <xsl:text>&#x3e;</xsl:text>
    </xsl:template>
    
    <xsl:template match="itst:builtIn" mode="javaType">
        <xsl:value-of select="@javaType"/>
    </xsl:template>

    <xsl:template match="xsd:simpleType|xsd:complexType|xsd:element[not(@nillable)]" mode="javaHolderType">
        <xsl:text>javax.xml.ws.Holder&#x3c;</xsl:text>
        <xsl:apply-templates select="." mode="javaType"/>
        <xsl:text>&#x3e;</xsl:text>
    </xsl:template>
    
    <xsl:template match="xsd:simpleType|xsd:complexType|xsd:element[not(@nillable)]" mode="javaType">
        <xsl:value-of select="@name"/>
    </xsl:template>


</xsl:stylesheet>
