<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xslt" version="1.0">

<xsl:output method="xml" omit-xml-declaration="no" indent="yes" xalan:indent-amount="2"/>
<xsl:strip-space elements="@*|node()"/>

<xsl:template match="@*|node()">
  <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>
