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
<![CDATA[
package org.objectweb.celtix.systest.type_test;

//import java.net.URI;
import javax.xml.ws.Holder;

/**
 * org.objectweb.celtix.systest.type_test.TypeTestImpl
 */
public class TypeTestImpl {

    public void testVoid() {
    }
    
    public void testOneway(String x, String y) {
    }
/*
    public AnonTypeElement testAnonTypeElement(AnonTypeElement x, 
            javax.xml.ws.Holder<AnonTypeElement> y, 
            javax.xml.ws.Holder<AnonTypeElement> z) {
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
    
    public String testSimpleRestriction(String x, 
                                        javax.xml.ws.Holder<String> y, 
                                        javax.xml.ws.Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction2(String x, 
                                         javax.xml.ws.Holder<String> y, 
                                         javax.xml.ws.Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction3(String x, 
                                         javax.xml.ws.Holder<String> y, 
                                         javax.xml.ws.Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction4(String x, 
                                         javax.xml.ws.Holder<String> y, 
                                         javax.xml.ws.Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction5(String x, 
                                         javax.xml.ws.Holder<String> y, 
                                         javax.xml.ws.Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String testSimpleRestriction6(String x, 
                                         javax.xml.ws.Holder<String> y, 
                                         javax.xml.ws.Holder<String> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public URI testAnyURIRestriction(URI x, javax.xml.ws.Holder<URI> y, javax.xml.ws.Holder<URI> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public byte[] testHexBinaryRestriction(byte[] x,
                                           javax.xml.ws.Holder<ByteArray> y, 
                                           javax.xml.ws.Holder<ByteArray> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public byte[] testBase64BinaryRestriction(byte[] x, 
                        javax.xml.ws.Holder<ByteArray> y, 
                        javax.xml.ws.Holder<ByteArray> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testSimpleListRestriction2(String[] x,
                        javax.xml.ws.Holder<ArrayOfString> y, 
                        javax.xml.ws.Holder<ArrayOfString> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }

    public String[] testStringList(String[] x,
                        javax.xml.ws.Holder<ArrayOfString> y,
                        javax.xml.ws.Holder<ArrayOfString> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public int[] testNumberList(int[] x,
                        javax.xml.ws.Holder<int[]> y,
                        javax.xml.ws.Holder<ArrayOfint> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public javax.xml.namespace.QName[] testQNameList(
                       javax.xml.namespace.QName[] x,
                       javax.xml.ws.Holder<ArrayOfQName> y,
                       javax.xml.ws.Holder<ArrayOfQName> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public ShortEnum[] testShortEnumList(ShortEnum[] x,
                       javax.xml.ws.Holder<ArrayOfShortEnum> y,
                       javax.xml.ws.Holder<ArrayOfShortEnum> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public AnonEnumListItemType[] testAnonEnumList(AnonEnumListItemType[] x,
                       javax.xml.ws.Holder<ArrayOfAnonEnumListItemType> y,
                       javax.xml.ws.Holder<ArrayOfAnonEnumListItemType> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public SimpleUnion[] testSimpleUnionList(SimpleUnion[] x,
                       javax.xml.ws.Holder<ArrayOfSimpleUnion> y,
                       javax.xml.ws.Holder<ArrayOfSimpleUnion> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    public AnonUnionListItemType[] testAnonUnionList(AnonUnionListItemType[] x,
                       javax.xml.ws.Holder<ArrayOfAnonUnionListItemType> y,
                       javax.xml.ws.Holder<ArrayOfAnoniUnionListItemType> z) {
        z.value = y.value;
        y.value = x;
        return x;
    }
*/    
    ]]>
        <xsl:apply-templates mode="definitions"/>
<![CDATA[
}
]]>
    </xsl:template>

    <xsl:template match="itst:it_test_group" mode="definitions">
        <!--xsl:apply-templates select="xsd:simpleType[not(
                @name='StringList'
                or @name='NumberList'
                or @name='QNameList'
                or @name='ShortEnumList'
                or @name='AnonEnumList'
                or @name='SimpleUnionList'
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
                or @name='AnonUnionList')]"
            mode="definition"/>
        <xsl:apply-templates select="xsd:complexType" mode="definition"/>
        <xsl:apply-templates select="xsd:element[not(@name='AnonTypeElement')]" mode="definition"/-->
        <xsl:apply-templates select="itst:builtIn" mode="definition"/>

    </xsl:template>

    <xsl:template
            match="itst:it_test_group/*[not(@itst:it_no_test='true')]"
            mode="definition">
        <xsl:apply-templates select="." mode="test_signature"/>
    <xsl:text> {    
        z.value = y.value;
        y.value = x;
        return x;
    }
    
    </xsl:text>
    </xsl:template>

</xsl:stylesheet>
