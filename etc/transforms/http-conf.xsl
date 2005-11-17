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

<!-- Generate http transport configuration metadata 
     using the type definitions in schema https://celtix.objectweb.org/transports/http/configuration
     and the element definitions in the (generated) schema https://celtix.objectweb.org/bus/transports/http/configuration/types
-->

    <xsl:output method="xml"/>
    
    <xsl:param name="type" value="server"/>

    <xsl:template match="/">
        <xsl:apply-templates select="xs:schema/xs:complexType"/>
    </xsl:template>

    <xsl:template match="xs:schema/xs:complexType">
        <xsl:if test="@name=concat($type,'Type')">
            <xsl:text>
</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;cm:config</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">xmlns:cm=&quot;http://celtix.objectweb.org/configuration/metadata&quot;</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">xmlns:std-types=&quot;http://celtix.objectweb.org/configuration/types&quot;</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">xmlns:http-conf=&quot;http://celtix.objectweb.org/transports/http/configuration&quot;</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">xmlns:http-conf-types=&quot;http://celtix.objectweb.org/bus/transports/http/configuration/types&quot;</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">xmlns:xs=&quot;http://www.w3.org/2001/XMLSchema&quot;</xsl:text><xsl:text>
    </xsl:text>
            <xsl:text disable-output-escaping="yes">namespace=&quot;http://celtix.objectweb.org/bus/transports/http/http-</xsl:text>
            <xsl:value-of select="$type"/><xsl:text disable-output-escaping="yes">-config&quot;</xsl:text><xsl:text>
</xsl:text>
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
            <xsl:text>

    </xsl:text>
            <xsl:element name="cm:configImport">
                <xsl:attribute name="namespace">
                    <xsl:text>http://celtix.objectweb.org/configuration/types</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="location">
                   <xsl:text>/org/objectweb/celtix/configuration/config-metadata/types.xsd</xsl:text>
                </xsl:attribute>
            </xsl:element>
            <xsl:text>
    </xsl:text>
            <xsl:element name="cm:configImport">
                <xsl:attribute name="namespace">
                    <xsl:text>http://celtix.objectweb.org/transports/http/configuration</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="location">
                   <xsl:text>http-conf.xsd</xsl:text>
                </xsl:attribute>
            </xsl:element>
            <xsl:text>
    </xsl:text>
            <xsl:element name="cm:configImport">
                <xsl:attribute name="namespace">
                    <xsl:text>http://celtix.objectweb.org/bus/transports/http/configuration/types</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="location">
                   <xsl:text>/org/objectweb/celtix/bus/transports/http/config-metadata/http-conf-types.xsd</xsl:text>
                </xsl:attribute>
            </xsl:element>
            <xsl:text>


</xsl:text>
            <xsl:apply-templates select="xs:complexContent/xs:extension/xs:attribute"/>
            <xsl:text>

</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;/cm:config&gt;</xsl:text>
            <xsl:text>
</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="xs:extension/xs:attribute">
         <xsl:text>

    </xsl:text>
        <xsl:element name="cm:configItem">
            <xsl:text>
        </xsl:text>
            <xsl:element name="cm:name">
               <xsl:value-of select="@name"/>
            </xsl:element>
            <xsl:text>
        </xsl:text>
            <xsl:element name="cm:type">
                <xsl:value-of select="@type"/>
            </xsl:element>
            <xsl:apply-templates select="xs:annotation/xs:documentation"/>
            <xsl:if test="boolean(@default)">
                <xsl:call-template name="defaultValue">
                    <xsl:with-param name="namespace-prefix" select="substring-before(@type,':')"/>
                    <xsl:with-param name="type" select="substring-after(@type,':')"/>
                    <xsl:with-param name="value" select="@default"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:text>
    </xsl:text>
        </xsl:element>
    </xsl:template>

    <xsl:template match="xs:annotation/xs:documentation">
        <xsl:text>
        </xsl:text>
        <xsl:element name="cm:description"><xsl:value-of select="."/></xsl:element><xsl:text>
        </xsl:text>
    </xsl:template>

    <xsl:template name="defaultValue">
        <xsl:param name="namespace-prefix"/>
        <xsl:param name="type"/>
        <xsl:param name="value"/>
        <xsl:text>
        </xsl:text>
        <xsl:call-template name="defaultValueElement">
            <xsl:with-param name="namespace-prefix" select="$namespace-prefix"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="mode" select="'begin'"/>
        </xsl:call-template>
        <xsl:value-of select="$value"/>
        <xsl:call-template name="defaultValueElement">
            <xsl:with-param name="namespace-prefix" select="$namespace-prefix"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="mode" select="'end'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="defaultValueElement">
        <xsl:param name="namespace-prefix"/>
        <xsl:param name="type"/>
        <xsl:param name="mode"/>
        <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
        <xsl:if test="$mode='end'">
            <xsl:text>/</xsl:text>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="$namespace-prefix='xs'">
                <xsl:text>std-types:</xsl:text><xsl:value-of select="$type"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>http-conf-types:</xsl:text><xsl:value-of select="$type"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text disable-output-escaping="yes">Value&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="text()|@*">
        <!-- do nothing -->
    </xsl:template>

</xsl:stylesheet>
