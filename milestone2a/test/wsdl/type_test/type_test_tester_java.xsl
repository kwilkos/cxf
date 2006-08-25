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

    <xsl:template match="/xsd:schema">
<![CDATA[
package org.objectweb.celtix.systest.type_test;

/**
 * org.objectweb.type_test.TypeTestTester
 */
public interface TypeTestTester {

    void setPerformanceTestOnly();
    
    /*void testAnonTypeElement() throws Exception;*/]]>
    
    void testVoid() throws Exception;
    
    void testOneway() throws Exception;
        <xsl:apply-templates mode="definitions"/>
        <![CDATA[
}]]>
    </xsl:template>

    <xsl:template match="itst:it_test_group" mode="definitions">
        <!--xsl:apply-templates select="xsd:simpleType" mode="definition"/>
        <xsl:apply-templates select="xsd:complexType" mode="definition"/>
        <xsl:apply-templates select="xsd:element[not(@name='AnonTypeElement')]" mode="definition"/-->
        <xsl:apply-templates select="itst:builtIn" mode="definition"/>
    </xsl:template>

    <xsl:template
            match="itst:it_test_group/*[not(@itst:it_no_test='true')]"
            mode="definition">
        <xsl:text>
    void test</xsl:text>
        <xsl:value-of select="concat(translate(substring(@name, 1, 1),
                                     'abcdefghijklmnopqrstuvwxyz', 
                                     'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
                                     substring(@name, 2))"/>
        <xsl:text>() throws Exception;</xsl:text>
        <xsl:text>
        </xsl:text>
    </xsl:template>

</xsl:stylesheet>
