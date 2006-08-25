<?xml version="1.0"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
    xmlns:wsse="http://schemas.xmlsoap.org/ws/2003/06/secext"
    xmlns:x1="http://objectweb.org/type_test/types"
    xmlns:itst="http://tests.iona.com/ittests">

  <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>
  <xsl:strip-space elements="*"/>

  <!-- Parameter: Path to the generated XSDs to include -->
  <xsl:param name="inc_xsd_path"/>
  <!-- Parameter: Use document-literal 'document' or rpc-literal 'rpc' style -->
  <xsl:param name="use_style"/>
 
  <!-- copy attributes from any node -->
  <xsl:template match="@*" mode="attribute_copy">
    <xsl:attribute name="{name(.)}">
      <xsl:value-of select="."/> 
    </xsl:attribute>
  </xsl:template>

  <!-- 0 - root schema node -->
  <xsl:template match="/xsd:schema">
    <xsl:if test="$use_style='document'">
      <definitions
          xmlns="http://schemas.xmlsoap.org/wsdl/"
          xmlns:tns="http://objectweb.org/type_test/doc"
          targetNamespace="http://objectweb.org/type_test/doc"
          name="type_test">
        <xsl:apply-templates select="@*[name(.)!='elementFormDefault']" mode="attribute_copy"/>
        <xsl:apply-templates select="." mode="schema"/>
        <xsl:apply-templates select="." mode="test_messages"/>
        <xsl:apply-templates select="." mode="test_portType"/>
      </definitions>
    </xsl:if>
    <xsl:if test="$use_style='rpc'">
      <definitions
          xmlns="http://schemas.xmlsoap.org/wsdl/"
          xmlns:tns="http://objectweb.org/type_test/rpc"
          targetNamespace="http://objectweb.org/type_test/rpc"
          name="type_test">
        <xsl:apply-templates select="@*" mode="attribute_copy"/>
        <xsl:apply-templates select="." mode="schema"/>
        <xsl:apply-templates select="." mode="test_messages"/>
        <xsl:apply-templates select="." mode="test_portType"/>
      </definitions>
    </xsl:if>
  </xsl:template>

  <!-- 1 - schema -->
  <xsl:template match="/xsd:schema" mode="schema"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <types>
      <xsd:schema xmlns="http://www.w3.org/2001/XMLSchema"
                  xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
                  jaxb:version="2.0">
        <xsl:attribute name="targetNamespace">
          <xsl:if test="$use_style='document'">
            <xsl:value-of select="'http://objectweb.org/type_test/doc'"/>
          </xsl:if>
          <xsl:if test="$use_style='rpc'">
            <xsl:value-of select="'http://objectweb.org/type_test/rpc'"/>
          </xsl:if>
        </xsl:attribute>
        <!-- 
        <xsd:annotation>
          <xsd:appinfo>
            <jaxb:globalBindings localScoping="nested">
        -->
              <!-- typesafeEnumBase="xsd:string xsd:byte xsd:short xsd:int xsd:long xsd:unsignedShort xsd:unsignedInt xsd:unsignedLong xsd:float xsd:double xsd:unsignedByte xsd:boolean xsd:integer xsd:positiveInteger xsd:nonPositiveInteger xsd:negativeInteger xsd:nonNegativeInteger xsd:decimal xsd:anyURI xsd:normalizedString xsd:token xsd:language xsd:NMTOKEN xsd:Name xsd:NCName" -->
              <!-- generateIsSetMethod="true" -->
              <!-- optionalProperty="isSet" -->
              <!-- jaxb:javaType name="java.net.URI" xmlType="xsd:anyURI" parseMethod="" printMethod=""/ -->
        <!-- 
            </jaxb:globalBindings>
          </xsd:appinfo>
        </xsd:annotation>
        -->
        <xsl:apply-templates select="@*" mode="attribute_copy"/>
        <xsl:apply-templates select="itst:it_test_group[@ID]" mode="schema_include"/>
        <xsl:apply-templates select="itst:it_test_group[not(@ID)]" mode="hardcoded_types"/>
        <xsl:apply-templates select="itst:it_test_group" mode="test_elements"/>
        <xsl:apply-templates select="itst:it_test_group[not(@ID)]" mode="schema_types"/>
      </xsd:schema>
    </types>
  </xsl:template>

  <!-- 1.1 - group of tests - schema include -->
  <xsl:template match="itst:it_test_group[@ID]" mode="schema_include">
    <xsd:import>
      <xsl:attribute name="namespace">
        <xsl:value-of select="'http://objectweb.org/type_test/types'"/>
      </xsl:attribute>
      <xsl:attribute name="schemaLocation">
        <xsl:value-of select="concat($inc_xsd_path, '/type_test_', @ID, '.xsd')"/> 
      </xsl:attribute>
    </xsd:import>
  </xsl:template>

  <!-- 1.2 - group of tests - schema include -->
  <xsl:template match="itst:it_test_group[not(@ID)]" mode="hardcoded_types">
    <!--
    <xsl:if test="$use_style='document'">
      <xsd:element name="testVoid"/>
      <xsd:element name="testOneway">
        <xsd:complexType>
          <sequence>
            <element name="x" type="xsd:string"/>
            <element name="y" type="xsd:string"/>
          </sequence>
        </xsd:complexType>
      </xsd:element>
    </xsl:if>
    -->
  </xsl:template>
  
  <!-- 1.3 group of types (only for groups with no ID) -->
  <xsl:template match="itst:it_test_group[not(@ID)]" mode="schema_types">
    <xsl:apply-templates select="xsd:attribute" mode="schema_type"/>
    <xsl:apply-templates select="xsd:attributeGroup" mode="schema_type"/>
    <xsl:apply-templates select="xsd:group" mode="schema_type"/>
    <xsl:apply-templates select="xsd:simpleType" mode="schema_type"/>
    <xsl:apply-templates select="xsd:complexType" mode="schema_type"/>
    <xsl:apply-templates select="xsd:element" mode="schema_type"/>
  </xsl:template>
  
  <!-- 1.3.1 - schema type or construct -->
  <xsl:template match="itst:it_test_group/*" mode="schema_type" xmlns="http://www.w3.org/2001/XMLSchema">
      <xsl:element name="{name(.)}">
        <!-- drop "it_no_test" from the attributes -->
        <xsl:apply-templates select="@*[name()!='itst:it_no_test']" mode="attribute_copy"/>
        <xsl:copy-of select="*"/>
      </xsl:element>
  </xsl:template>

  <!-- 1.4 - group of tests - test elements -->
  <xsl:template match="itst:it_test_group" mode="test_elements">
    <xsl:if test="$use_style='document'">
      <xsl:apply-templates select="xsd:simpleType" mode="elements_xyz">
          <xsl:with-param name="prefix">x1:</xsl:with-param>
      </xsl:apply-templates>
      <xsl:apply-templates select="xsd:complexType" mode="elements_xyz">
          <xsl:with-param name="prefix">x1:</xsl:with-param>
      </xsl:apply-templates>
      <!--
      <xsl:apply-templates select="xsd:element" mode="elements_xyz">
          <xsl:with-param name="prefix">x1:</xsl:with-param>
      </xsl:apply-templates>
      -->
      <xsl:apply-templates select="itst:builtIn" mode="elements_xyz">
          <xsl:with-param name="prefix">xsd:</xsl:with-param>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <!-- 1.4.1 - group of x/y/z/return doc-literal test elements -->
  <xsl:template match="itst:it_test_group/*[not(@itst:it_no_test='true')]" mode="elements_xyz">
    <xsl:param name="prefix"/>
    <xsl:param name="operation_name">
      <xsl:value-of select="concat('test',
                            concat(translate(substring(@name, 1, 1),    
                                   'abcdefghijklmnopqrstuvwxyz', 
                                   'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
                                   substring(@name, 2)))"/>
    </xsl:param>
      <xsd:element>
        <xsl:attribute name="name">
          <xsl:value-of select="$operation_name"/>
        </xsl:attribute>
        <xsl:apply-templates select="." mode="elements_in">
          <xsl:with-param name="prefix">
            <xsl:value-of select="$prefix"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsd:element>
      <xsd:element>
        <xsl:attribute name="name">
          <xsl:value-of select="concat($operation_name, 'Response')"/>
        </xsl:attribute>
        <xsl:apply-templates select="." mode="elements_out">
          <xsl:with-param name="prefix">
            <xsl:value-of select="$prefix"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsd:element>
  </xsl:template>

  <!-- 1.4.1.1 - group of x/y test elements -->
  <xsl:template match="itst:it_test_group/*[not(@itst:it_no_test='true')]" mode="elements_in">
    <xsl:param name="prefix"/>
    <xsd:complexType>
      <sequence>
        <xsd:element>
          <xsl:attribute name="name">x</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </xsd:element>
        <xsd:element>
          <xsl:attribute name="name">y</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </xsd:element>
      </sequence>
    </xsd:complexType>
  </xsl:template>

  <!-- 1.4.1.2 - group of y/z/return test elements -->
  <xsl:template match="itst:it_test_group/*[not(@itst:it_no_test='true')]" mode="elements_out">
    <xsl:param name="prefix"/>
    <xsd:complexType>
      <sequence>
        <xsd:element>
          <xsl:attribute name="name">return</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </xsd:element>
        <xsd:element>
          <xsl:attribute name="name">y</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </xsd:element>
        <xsd:element>
          <xsl:attribute name="name">z</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </xsd:element>
      </sequence>
    </xsd:complexType>
  </xsl:template>

  <!-- 2 - test messages -->
  <xsl:template match="/xsd:schema" mode="test_messages">
    <xsl:apply-templates select="." mode="hardcoded_messages"/>
    <xsl:apply-templates select="itst:it_test_group" mode="test_messages_group"/>
  </xsl:template>

  <!-- 2.1 - hardcoded messages -->
  <xsl:template match="/xsd:schema" mode="hardcoded_messages"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <!--
    <message name="testVoid">
        <xsl:if test="$use_style='document'">
          <part name="in" element="tns:testVoid"/>
        </xsl:if>
    </message>
    <message name="testOneway">
        <xsl:if test="$use_style='document'">
          <part name="in" element="tns:testOneway"/>
        </xsl:if>
        <xsl:if test="$use_style='rpc'">
          <part name="x" type="xsd:string"/>
          <part name="y" type="xsd:string"/>
        </xsl:if>
    </message>
    -->
  </xsl:template>

  <!-- 2.2 - group of test messages -->
  <xsl:template match="itst:it_test_group" mode="test_messages_group">
    <xsl:apply-templates select="xsd:simpleType" mode="test_messages">
      <xsl:with-param name="prefix">x1:</xsl:with-param>
    </xsl:apply-templates>
    <xsl:apply-templates select="xsd:complexType" mode="test_messages">
      <xsl:with-param name="prefix">x1:</xsl:with-param>
    </xsl:apply-templates>
    <!--
    <xsl:apply-templates select="xsd:element" mode="test_messages">
      <xsl:with-param name="prefix">x1:</xsl:with-param>
    </xsl:apply-templates>
    -->
    <xsl:apply-templates select="itst:builtIn" mode="test_messages">
      <xsl:with-param name="prefix">xsd:</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- 2.2.1 - request and response messages -->
  <xsl:template match="itst:it_test_group/*[not(@itst:it_no_test='true')]" 
        mode="test_messages" xmlns="http://schemas.xmlsoap.org/wsdl/">
    <!-- prefix is used for rpc style -->
    <xsl:param name="prefix"/>
    <xsl:variable name="message_name_prefix">
      <xsl:value-of select="concat('test',
                                   concat(translate(substring(@name, 1, 1),
                                          'abcdefghijklmnopqrstuvwxyz', 
                                          'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
                                          substring(@name, 2)))"/>
    </xsl:variable>
    <xsl:variable name="input_message_name">
      <xsl:value-of select="$message_name_prefix"/>
    </xsl:variable>
    <xsl:variable name="output_message_name">
      <xsl:value-of select="concat($message_name_prefix, 'Response')"/>
    </xsl:variable>
    <message>
      <xsl:attribute name="name">
        <xsl:value-of select="$input_message_name"/>
      </xsl:attribute>
      <xsl:if test="$use_style='document'">
        <part>
          <xsl:attribute name="name">in</xsl:attribute>
          <xsl:attribute name="element">
            <xsl:value-of select="concat('tns:', $message_name_prefix)"/>
          </xsl:attribute>
        </part>
      </xsl:if>
      <xsl:if test="$use_style='rpc'">
        <part>
          <xsl:attribute name="name">x</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </part>
        <part>
          <xsl:attribute name="name">y</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </part>
      </xsl:if>
    </message>
    <message>
      <xsl:attribute name="name">
        <xsl:value-of select="$output_message_name"/>
      </xsl:attribute>
      <xsl:if test="$use_style='document'">
        <part>
          <xsl:attribute name="name">out</xsl:attribute>
          <xsl:attribute name="element">
            <xsl:value-of select="concat('tns:', $output_message_name)"/>
          </xsl:attribute>
        </part>
      </xsl:if>
      <xsl:if test="$use_style='rpc'">
        <part>
          <xsl:attribute name="name">return</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </part>
        <part>
          <xsl:attribute name="name">y</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </part>
        <part>
          <xsl:attribute name="name">z</xsl:attribute>
          <xsl:attribute name="type">
            <xsl:value-of select="concat($prefix, @name)"/>
          </xsl:attribute>
        </part>
      </xsl:if>
    </message>
  </xsl:template>

  <!-- 3 - test portType -->
  <xsl:template match="/xsd:schema" mode="test_portType"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <portType name="TypeTestPortType">
      <xsl:apply-templates select="." mode="hardcoded_operations"/>
      <xsl:apply-templates select="itst:it_test_group" mode="test_operations_group"/>
    </portType>
  </xsl:template>

  <!-- 3.1 - hardcoded operations -->
  <xsl:template match="/xsd:schema" mode="hardcoded_operations"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <!--
    <operation name="testVoid">
      <input message="testVoid" name="tns:testVoid"/>
    </operation>
    <operation name="testOneway">
      <xsl:if test="$use_style='rpc'">
        <xsl:attribute name="parameterOrder">
          <xsl:value-of select="'x y'"/>
        </xsl:attribute>
      </xsl:if>
      <input message="testOneway" name="tns:testOneway"/>
    </operation>
    -->
  </xsl:template>

  <!-- 3.2 - group of test operations -->
  <xsl:template match="itst:it_test_group" mode="test_operations_group">
    <xsl:apply-templates select="xsd:simpleType" mode="test_operation"/>
    <xsl:apply-templates select="xsd:complexType" mode="test_operation"/>
    <!--
    <xsl:apply-templates select="xsd:element" mode="test_operation"/>
    -->
    <xsl:apply-templates select="itst:builtIn" mode="test_operation"/>
  </xsl:template>
  
  <!-- 3.2.1 - test operations -->
  <xsl:template match="itst:it_test_group/*[not(@itst:it_no_test='true')]" mode="test_operation"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <xsl:variable name="operation_name">
      <xsl:value-of select="concat('test',
                                   concat(translate(substring(@name, 1, 1),
                                          'abcdefghijklmnopqrstuvwxyz', 
                                          'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
                                          substring(@name, 2)))"/>
    </xsl:variable>
    <xsl:variable name="operation_input_name">
      <xsl:value-of select="$operation_name"/>
    </xsl:variable>
    <xsl:variable name="operation_output_name">
      <xsl:value-of select="concat($operation_name, 'Response')"/>
    </xsl:variable>
    <xsl:variable name="input_message_name">
      <xsl:value-of select="concat('tns:', $operation_input_name)"/>
    </xsl:variable>
    <xsl:variable name="output_message_name">
      <xsl:value-of select="concat('tns:', $operation_output_name)"/>
    </xsl:variable>
    <operation>
      <xsl:attribute name="name">
        <xsl:value-of select="$operation_name"/>
      </xsl:attribute>
      <xsl:if test="$use_style='rpc'">
        <xsl:attribute name="parameterOrder">x y z</xsl:attribute>
      </xsl:if>
      <input>
        <xsl:attribute name="name">
          <xsl:value-of select="$operation_input_name"/>
        </xsl:attribute>
        <xsl:attribute name="message">
          <xsl:value-of select="$input_message_name"/>
        </xsl:attribute>
      </input>
      <output>
        <xsl:attribute name="name">
          <xsl:value-of select="$operation_output_name"/>
        </xsl:attribute>
        <xsl:attribute name="message">
          <xsl:value-of select="$output_message_name"/>
        </xsl:attribute>
      </output>
    </operation>
  </xsl:template>

</xsl:stylesheet>

