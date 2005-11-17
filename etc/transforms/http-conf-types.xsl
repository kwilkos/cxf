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

<!-- Generate the https://celtix.objectweb.org/bus/transports/http/configuration/types schema 
     it imports the https://celtix.objectweb.org/transports/http/configuration/ schema
     and defines global elements that can be used as default values in the 
     http transport configuration metadata model 
-->

    <xsl:output method="xml"/>
   
    <xsl:template match="/">
        <xsl:text>

</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;xs:schema</xsl:text><xsl:text>
    </xsl:text>
        <xsl:text disable-output-escaping="yes">xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot;</xsl:text><xsl:text>
    </xsl:text>
        <xsl:text disable-output-escaping="yes">xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;</xsl:text><xsl:text>
    </xsl:text>
        <xsl:text disable-output-escaping="yes">xmlns:http-conf=&quot;http://celtix.objectweb.org/transports/http/configuration&quot;</xsl:text><xsl:text>
    </xsl:text>           
        <xsl:text disable-output-escaping="yes">xmlns:tns=&quot;http://celtix.objectweb.org/bus/transports/http/configuration/types&quot;</xsl:text><xsl:text>
    </xsl:text>           
        <xsl:text disable-output-escaping="yes">targetNamespace=&quot;http://celtix.objectweb.org/bus/transports/http/configuration/types&quot;</xsl:text><xsl:text>
</xsl:text>           
        <xsl:text disable-output-escaping="yes">&gt;</xsl:text><xsl:text>


    </xsl:text>
        <xsl:element name="xs:import">
            <xsl:attribute name="namespace">
                <xsl:text>http://celtix.objectweb.org/transports/http/configuration</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="schemaLocation">
                <xsl:text>http-conf.xsd</xsl:text>
            </xsl:attribute>
        </xsl:element><xsl:text>

</xsl:text>

        <xsl:apply-templates select="xs:schema/xs:simpleType"/>

        <xsl:text>
</xsl:text>

        <xsl:text disable-output-escaping="yes">&lt;/xs:schema&gt;</xsl:text><xsl:text>
</xsl:text>

    </xsl:template>

    <xsl:template match="xs:schema/xs:simpleType">
        <xsl:text>
    </xsl:text>
        <xsl:element name="xs:element">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/><xsl:text>Value</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="type">
                <xsl:value-of select="concat('http-conf:',@name)"/>
            </xsl:attribute>
        
        </xsl:element>
        <!--
        <xsl:text>

    </xsl:text>
        <xsl:element name="xs:simpleType">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute
            <xsl:apply-templates select="node()|@*"/>
        </xsl:element>
        <xsl:text>
    </xsl:text>
        -->
        <xsl:text>

</xsl:text>
    </xsl:template>

    <xsl:template match="node()|@*">
       <xsl:copy>
           <xsl:apply-templates select="node()|@*"/>
       </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
