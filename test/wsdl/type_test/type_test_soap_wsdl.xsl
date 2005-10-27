<?xml version="1.0"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
    xmlns:tns="http://objectweb.org/type_test"
    xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
    xmlns:itst="http://tests.iona.com/ittests"
    xmlns:http-conf="http://schemas.iona.com/transports/http/configuration">

  <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>
  <xsl:strip-space elements="*"/>

  <!-- Parameter: Path to the generated type_test WSDL to include -->
  <xsl:param name="inc_wsdl_path"/>
  <!-- Parameter: Use DOC-literal or RPC-literal style -->
  <xsl:param name="use_style"/>
 
  <!-- copy attributes from any node -->
  <xsl:template match="@*" mode="attribute_copy">
    <xsl:attribute name="{name(.)}">
      <xsl:value-of select="."/> 
    </xsl:attribute>
  </xsl:template>

  <!-- 0 - root schema node -->
  <xsl:template match="/xsd:schema">
    <wsdl:definitions
        xmlns="http://schemas.xmlsoap.org/wsdl/"
        name="type_test_soap">
      <xsl:apply-templates select="@*" mode="attribute_copy"/>
      <xsl:apply-templates select="." mode="test_binding"/>
   </wsdl:definitions>
  </xsl:template>

  <!-- 1 - test binding -->
  <xsl:template match="/xsd:schema" mode="test_binding"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <wsdl:import>
      <xsl:attribute name="namespace">
        <xsl:value-of select="'http://objectweb.org/type_test'"/>
      </xsl:attribute>
      <xsl:attribute name="location">
        <xsl:value-of select="concat($inc_wsdl_path, '/type_test.wsdl')"/> 
      </xsl:attribute>
    </wsdl:import>
    <wsdl:binding type="tns:TypeTestPortType">
      <xsl:attribute name="name">
        <xsl:if test="$use_style='document'">
            <xsl:value-of select="concat('TypeTestSOAP', 'DocLit')"/>
        </xsl:if>
        <xsl:if test="$use_style='rpc'">
            <xsl:value-of select="concat('TypeTestSOAP', 'RpcLit')"/>
        </xsl:if>
      </xsl:attribute>
      <soap:binding transport="http://schemas.xmlsoap.org/soap/http">
        <xsl:attribute name="style">
          <xsl:value-of select="$use_style"/>
        </xsl:attribute>
      </soap:binding>
      <xsl:apply-templates select="." mode="hardcoded_operations"/>
      <xsl:apply-templates select="itst:it_test_group" mode="test_operations_group"/>
    </wsdl:binding>
    <wsdl:service name="SOAPService">
      <wsdl:port name="SOAPPort">
        <xsl:attribute name="binding" xmlns="http://schemas.xmlsoap.org/">
          <xsl:if test="$use_style='document'">
            <xsl:value-of select="concat('tns:TypeTestSOAP', 'DocLit')"/> 
          </xsl:if>
          <xsl:if test="$use_style='rpc'">
            <xsl:value-of select="concat('tns:TypeTestSOAP', 'RpcLit')"/> 
          </xsl:if>
        </xsl:attribute>
        <soap:address location="http://localhost:9200/SOAPService/SOAPPort/"/>
          <http-conf:client SendTimeout="120000" ReceiveTimeout="180000"/>
          <http-conf:server SendTimeout="120000" ReceiveTimeout="180000"/>
      </wsdl:port>
    </wsdl:service>
  </xsl:template>

  <!-- 1.1 - hardcoded operations -->
  <xsl:template match="/xsd:schema" mode="hardcoded_operations"
        xmlns="http://schemas.xmlsoap.org/wsdl/">
    <!--
    <wsdl:operation name="testVoid">
      <soap:operation soapAction="">
        <xsl:attribute name="style">
          <xsl:value-of select="$use_style"/>
        </xsl:attribute>
      </soap:operation>
      <wsdl:input>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="'testVoid'"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:input>
    </wsdl:operation>
    <wsdl:operation name="testOneway">
      <soap:operation soapAction="">
        <xsl:attribute name="style">
          <xsl:value-of select="$use_style"/>
        </xsl:attribute>
      </soap:operation>
      <wsdl:input>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="'testOneway'"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:input>
    </wsdl:operation>
    -->
    <!--
    <wsdl:operation name="testDispatch1">
      <soap:operation soapAction="">
        <xsl:attribute name="style">
          <xsl:value-of select="$use_style"/>
        </xsl:attribute>
      </soap:operation>
      <wsdl:input>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="'testDispatch1'"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:input>
      <wsdl:output>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="'testDispatch1Response'"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:output>
    </wsdl:operation>
    <wsdl:operation name="testDispatch2">
      <soap:operation soapAction="">
        <xsl:attribute name="style">
          <xsl:value-of select="$use_style"/>
        </xsl:attribute>
      </soap:operation>
      <wsdl:input>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="'testDispatch2'"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:input>
      <wsdl:output>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="'testDispatch2Response'"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:output>
    </wsdl:operation>
    -->
  </xsl:template>

  <!-- 1.2 - group of test operations -->
  <xsl:template match="itst:it_test_group" mode="test_operations_group">
    <!--xsl:apply-templates select="xsd:simpleType" mode="test_operation"/>
    <xsl:apply-templates select="xsd:complexType" mode="test_operation"/>
    <xsl:apply-templates select="xsd:element" mode="test_operation"/-->
    <xsl:apply-templates select="itst:builtIn" mode="test_operation"/>
  </xsl:template>
  
  <!-- 1.2.1 - test operations -->
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
    <wsdl:operation>
      <xsl:attribute name="name">
        <xsl:value-of select="$operation_name"/>
      </xsl:attribute>
      <soap:operation soapAction="">
        <xsl:attribute name="style">
          <xsl:value-of select="$use_style"/>
        </xsl:attribute>
      </soap:operation>
      <wsdl:input>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="$operation_input_name"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:input>
      <wsdl:output>
        <xsl:if test="$use_style='document'">
          <xsl:attribute name="name">
            <xsl:value-of select="$operation_output_name"/>
          </xsl:attribute>
        </xsl:if>
        <soap:body use="literal">
          <xsl:if test="$use_style='rpc'">
            <xsl:attribute name="namespace">
              <xsl:value-of select="'http://objectweb.org/type_test'"/>
            </xsl:attribute>
          </xsl:if>
        </soap:body>
      </wsdl:output>
    </wsdl:operation>
  </xsl:template>

</xsl:stylesheet>

