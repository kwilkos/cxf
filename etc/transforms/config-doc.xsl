<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:fn="http://www.w3.org/2005/02/xpath-functions" 
    xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes"
    xmlns:cm="http://celtix.objectweb.org/configuration/metadata"
>

<!-- Generate documentation from component configuration metadata 
-->

    <xsl:output method="html"/>
    
    <xsl:template match="cm:config">
<html>
<head>
    <title>Configuration for Component <xsl:value-of select="@namespace"/></title>
</head>
<body>
<h2>Configuration for Component <xsl:value-of select="@namespace"/></h2>
        <xsl:apply-templates select="cm:configItem"/>
</body>
</html>
    </xsl:template>


    <xsl:template match="cm:configItem">
<table border="0" cellspacing="5">
        <xsl:for-each select="./*">
            <xsl:choose>

                <xsl:when test="name(.)='cm:name'">
<tr>
<td style="width:200px;"><b><i>Variable</i></b></td>
<td style="width:600px;"><b>
                    <xsl:value-of select="."/>
</b></td>
</tr>
                </xsl:when>
                
                <xsl:when test="name(.)='cm:type'">
<tr>
<td style="width:200px;"><b><i>Type</i></b></td>
<td style="width:600px;">
                    <xsl:value-of select="."/>
</td>
</tr>
                </xsl:when>

                <xsl:when test="name(.)='cm:description'">
<tr>
<td style="width:200px;"><b><i>Description</i></b></td>
<td style="width:600px;">
                    <xsl:value-of select="."/>
</td>
</tr>
                </xsl:when>

                <xsl:when test="name(.)='cm:lifecyclePolicy'">
                    <!-- do nothing -->
                </xsl:when>

                <xsl:otherwise>
                    <!-- the default value -->
<tr>
<td style="width:200px;"><b><i>Default</i></b></td>
<td style="width:600px;">
<code>
                    <!-- do nothing -->
                    <xsl:apply-templates mode="copy" select="."/>
                    <!--
                    <xsl:call-template name="getDefaultValue">
                        </xsl:with-param name="value" select=".">
                    </xsl:call-template>
                    -->
</code>                    
</td>
</tr>
                </xsl:otherwise>

            </xsl:choose>
        </xsl:for-each>
</table>
<p>
</p>
    </xsl:template>

    <xsl:template name="getDefaultValue">
        <xsl:param name="value"/>
        <xsl:apply-templates mode="copy" select="$value"/>
    </xsl:template>

    <xsl:template match="*" mode="copy">
<br>
</br>
        <xsl:text diable-output-escaping="yes">&lt;</xsl:text>
        <xsl:value-of select="name(.)"/>
        <xsl:text diable-output-escaping="yes">&gt;</xsl:text>
        <xsl:apply-templates select="text()" mode="copy"/>
        <xsl:apply-templates select="*" mode="copy"/>
        <xsl:text diable-output-escaping="yes">&lt;/</xsl:text>
        <xsl:value-of select="name(.)"/>
        <xsl:text diable-output-escaping="yes">&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="text()" mode="copy">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
