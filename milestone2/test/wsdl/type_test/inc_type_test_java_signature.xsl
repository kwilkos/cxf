<!--
     Stylesheet to convert schema into java file for test implementation.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:itst="http://tests.iona.com/ittests">

    <xsl:import href="inc_type_test_java_types.xsl"/>

    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:template match="xsd:simpleType|xsd:complexType|itst:builtIn" mode="test_signature">
        <xsl:variable name="the_name">
            <xsl:value-of select="concat(translate(substring(@name, 1, 1),
                                         'abcdefghijklmnopqrstuvwxyz', 
                                         'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
                                         substring(@name, 2))"/>
        </xsl:variable>
        <xsl:variable name="operation_name">
            <xsl:value-of select="concat('test', $the_name)"/>
        </xsl:variable>
        <xsl:variable name="class_name">
            <xsl:value-of select="concat('Test', $the_name)"/>
        </xsl:variable>
        <xsl:text>
    @WebMethod(operationName = "</xsl:text>
        <xsl:value-of select="$operation_name"/>
        <xsl:text>")
    @RequestWrapper(className = "org.objectweb.type_test.</xsl:text>
        <xsl:value-of select="$class_name"/>
        <xsl:text>",
                    localName = "</xsl:text>
        <xsl:value-of select="$operation_name"/>
        <xsl:text>",
                    targetNamespace = "http://objectweb.org/type_test")
    @ResponseWrapper(className = "org.objectweb.type_test.</xsl:text>
        <xsl:value-of select="$class_name"/>
        <xsl:text>Response",
                     localName = "</xsl:text>
        <xsl:value-of select="$operation_name"/>
        <xsl:text>Response",
                     targetNamespace = "http://objectweb.org/type_test")
    public </xsl:text>
        <xsl:apply-templates select="." mode="javaType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$operation_name"/>
        <xsl:text>(
            @WebParam(name = "x", targetNamespace = "")
            </xsl:text>
        <xsl:apply-templates select="." mode="javaType"/>
        <xsl:text> x,
            @WebParam(name = "y", targetNamespace = "", mode = Mode.INOUT)</xsl:text>
        <xsl:apply-templates select="." mode="javaHolderType"/>
        <xsl:text> y,
            @WebParam(name = "z", targetNamespace = "", mode = Mode.OUT)</xsl:text>
        <xsl:apply-templates select="." mode="javaHolderType"/>
        <xsl:text> z)</xsl:text>
    </xsl:template>

    <xsl:template match="xsd:element" mode="test_signature">
        <xsl:text>
    public </xsl:text>
        <xsl:apply-templates select="." mode="javaType"/>
        <xsl:text> test_</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="." mode="javaType_suffix">
            <xsl:with-param name="suffix">_x</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text> x, </xsl:text>
        <xsl:apply-templates select="." mode="javaHolderType_suffix">
            <xsl:with-param name="suffix">_y</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text> y, </xsl:text>
        <xsl:apply-templates select="." mode="javaHolderType_suffix">
            <xsl:with-param name="suffix">_z</xsl:with-param>
        </xsl:apply-templates>
        <xsl:text> z)</xsl:text>
    </xsl:template>

</xsl:stylesheet>
