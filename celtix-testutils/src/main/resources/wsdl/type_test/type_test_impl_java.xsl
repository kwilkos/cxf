<!--
     Stylesheet to convert schema into java file for test implementation.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:itst="http://tests.iona.com/ittests">
    
    <xsl:import href="inc_type_test_java_signature.xsl"/>
    
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:template match="/xsd:schema">
      <xsl:text>package org.objectweb.celtix.systest.type_test;&#10;&#10;</xsl:text>
      <xsl:text>//import java.net.URI;&#10;</xsl:text>
      <xsl:text>import java.util.List;&#10;</xsl:text>
      <xsl:text>import javax.xml.ws.Holder;&#10;&#10;</xsl:text>
      <xsl:apply-templates select="itst:it_test_group[@ID]" mode="imports"/>
<![CDATA[/**
 * org.objectweb.celtix.systest.type_test.TypeTestImpl
 */
public class TypeTestImpl {

    public void testVoid() {
    }
    
    public void testOneway(String x, String y) {
    }
/*
    public AnonTypeElement testAnonTypeElement(AnonTypeElement x, 
            Holder<AnonTypeElement> y, 
            Holder<AnonTypeElement> z) {
        z.value.setVarFloat(y.value.getVarFloat());
        z.value.setVarInt(y.value.getVarInt());
        z.value.setVarString(y.value.getVarString());

        y.value.setVarFloat(x.getVarFloat());
        y.value.setVarInt(x.getVarInt());
        y.value.setVarString(x.getVarString());

        AnonTypeElement varReturn = new AnonTypeElement();
        varReturn.setVarFloat(x.getVarFloat());
        varReturn.setVarInt(x.getVarInt());
        varReturn.setVarString(x.getVarString());
        return varReturn;
    }
*/    
    public String testSimpleRestriction(String x, 
            Holder<String> y, 
            Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction2(String x, 
            Holder<String> y, 
            Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction3(String x, 
            Holder<String> y, 
            Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction4(String x, 
            Holder<String> y, 
            Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction5(String x, 
            Holder<String> y, 
            Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction6(String x, 
            Holder<String> y, 
            Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
/*
    public URI testAnyURIRestriction(URI x, Holder<URI> y, Holder<URI> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
*/
    public byte[] testHexBinaryRestriction(byte[] x,
            Holder<byte[]> y, 
            Holder<byte[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public byte[] testBase64BinaryRestriction(byte[] x, 
            Holder<byte[]> y, 
            Holder<byte[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public List<String> testSimpleListRestriction2(List<String> x,
            Holder< List<String> > y,
            Holder< List<String> > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public String[] testSimpleListRestriction2(String[] x,
            Holder<String[]> y, 
            Holder<String[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public List<String> testStringList(List<String> x,
            Holder< List<String> > y,
            Holder< List<String> > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public String[] testStringList(String[] x,
            Holder<String[]> y,
            Holder<String[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public List<java.lang.Integer> testNumberList(List<java.lang.Integer> x,
            Holder< List<java.lang.Integer> > y,
            Holder< List<java.lang.Integer> > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public java.lang.Integer[] testNumberList(java.lang.Integer[] x,
            Holder<java.lang.Integer[]> y,
            Holder<java.lang.Integer[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public List<javax.xml.namespace.QName> testQNameList(
            List<javax.xml.namespace.QName> x,
            Holder< List<javax.xml.namespace.QName> > y,
            Holder< List<javax.xml.namespace.QName> > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public javax.xml.namespace.QName[] testQNameList(
            javax.xml.namespace.QName[] x,
            Holder<javax.xml.namespace.QName[]> y,
            Holder<javax.xml.namespace.QName[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
/*    
    public ShortEnum[] testShortEnumList(ShortEnum[] x,
            Holder<ShortEnum[]> y,
            Holder<ShortEnum[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
*/
    public List<java.lang.Short> testAnonEnumList(
            List<java.lang.Short> x,
            Holder< List<java.lang.Short> > y,
            Holder< List<java.lang.Short> > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public java.lang.Short[] testAnonEnumList(java.lang.Short[] x,
            Holder<java.lang.Short[]> y,
            Holder<java.lang.Short[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testSimpleUnionList(String[] x,
            Holder<String[]> y,
            Holder<String[]> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
 
    public String[] testAnonUnionList(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public java.lang.String testID(
            java.lang.String x,
            Holder<java.lang.String> y,
            Holder<java.lang.String> z) {    
        z.value = y.value;
        y.value = x;
        return x + y.value;
    }

    public IDTypeAttribute testIDTypeAttribute(
            IDTypeAttribute x,
            Holder<IDTypeAttribute> y,
            Holder<IDTypeAttribute> z) {    
        z.value = y.value;
        y.value = x;
        IDTypeAttribute varReturn = new IDTypeAttribute();
        varReturn.setId(x.getId() + y.value.getId());
        return varReturn;
    }

    public String[] testUnionWithAnonList(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testUnionWithStringList(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testUnionWithStringListRestriction(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testUnionWithAnonUnion(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testUnionWithUnion(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testUnionWithUnionRestriction(String[] x,
            Holder< String[] > y,
            Holder< String[] > z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
]]>
      <xsl:apply-templates select="." mode="definitions"/>
<![CDATA[}]]>
    </xsl:template>

    <xsl:template match="itst:it_test_group" mode="imports">
        <xsl:apply-templates select="xsd:simpleType[not(
                @name='StringList'
                or @name='NumberList'
                or @name='QNameList'
                or @name='ShortEnumList'
                or @name='AnonEnumList'
                or @name='SimpleRestriction'
                or @name='SimpleRestriction2'
                or @name='SimpleRestriction3'
                or @name='SimpleRestriction4'
                or @name='SimpleRestriction5'
                or @name='SimpleRestriction6'
                or @name='AnyURIRestriction'
                or @name='HexBinaryRestriction'
                or @name='Base64BinaryRestriction'
                or @name='SimpleListRestriction2'
                or contains(@name, 'Union')
                or @itst:it_no_test='true')]"
                mode="import">
            <!--
                or @name='SimpleUnionList'
                or @name='AnonUnionList'
            -->
            <xsl:sort select="@name"/>
            <xsl:with-param name="id" select="@ID"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="xsd:complexType[not(
                @itst:it_no_test='true')]"
                mode="import">
            <xsl:with-param name="id" select="@ID"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="itst:it_test_group/*" mode="import">
        <xsl:param name="id"/>
        <xsl:value-of select="concat('import org.objectweb.type_test.types',
            $id, '.', @name, ';&#10;')"/>
    </xsl:template>

    <xsl:template match="itst:it_test_group" mode="definitions">
        <xsl:apply-templates select="xsd:simpleType[not(
                @name='StringList'
                or @name='NumberList'
                or @name='QNameList'
                or @name='ShortEnumList'
                or @name='AnonEnumList'
                or @name='SimpleRestriction'
                or @name='SimpleRestriction2'
                or @name='SimpleRestriction3'
                or @name='SimpleRestriction4'
                or @name='SimpleRestriction5'
                or @name='SimpleRestriction6'
                or @name='AnyURIRestriction'
                or @name='HexBinaryRestriction'
                or @name='Base64BinaryRestriction'
                or @name='SimpleListRestriction2'
                or @itst:it_no_test='true')]"
            mode="definition"/>
            <!--
                or @name='SimpleUnionList'
                or @name='AnonUnionList'
            -->
        <xsl:apply-templates select="xsd:complexType[not(
                @name='IDTypeAttribute'
                or @itst:it_no_test='true')]"
            mode="definition"/>
        <!-- xsl:apply-templates select="xsd:element[not(
                @name='AnonTypeElement'
                or @itst:it_no_test='true')]"
            mode="definition"/ -->
        <xsl:apply-templates select="itst:builtIn[not(
                @name='ID'
                or @itst:it_no_test='true')]"
            mode="definition"/>
    </xsl:template>

    <xsl:template match="itst:it_test_group/*" mode="definition">
        <xsl:apply-templates select="." mode="test_signature"/>
    <xsl:text> {    
        z.value = y.value;
        y.value = x;
        return x;
    }&#10;</xsl:text>
    </xsl:template>

</xsl:stylesheet>
