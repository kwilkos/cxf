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
<table border="0" cellspacing="5">
        <xsl:apply-templates select="cm:configItem"/>
</table>
</body>
</html>
    </xsl:template>


    <xsl:template match="cm:configItem">
<tr>
<td><b><i>Variable</i></b></td>
<td colspan="2"><b>
        <xsl:value-of select="cm:name"/>
</b></td>
</tr>
<tr>
<td><b><i>Type</i></b></td>
<td colspan="2">
        <xsl:value-of select="cm:type"/>
</td>
</tr>
<tr>
<td><b><i>Description</i></b></td>
<td colspan="2">
      <xsl:value-of select="cm:description"/>
</td>
</tr>
<tr>
<td><b><i>Lifecycle Policy</i></b></td>
<td colspan="2">
      <xsl:value-of select="cm:lifecyclePolicy"/>
</td>
</tr>
<tr>
<td><b><i>Default Value</i></b></td>
<td colspan="2">
<code>
      <xsl:value-of select="'-'"/>
</code>
</td>
</tr>
<tr>
<td><b><i>Sources</i></b><br></br></td>
<td colspan="2">
    <!--
    <xsl:choose>
    </xsl:choose>
    -->
    WSDL, Configuration<br></br></td>
</tr>
    </xsl:template>


</xsl:stylesheet>
