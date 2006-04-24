package org.objectweb.celtix.tools.common;

import javax.xml.namespace.QName;

public final class WSDLConstants {

    public static final String DOT_WSDL = ".wsdl";
    public static final String RESPONSE = "Response";
    public static final String PARAMETERS = "parameters";
    public static final String RESULT = "parameters";
    public static final String UNWRAPPABLE_RESULT = "result";

    public static final String WSDL_PREFIX = "wsdl";
    public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final String XSD_PREFIX = "xsd";
    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String SOAP_PREFIX = "soap";
    public static final String SOAP12_PREFIX = "soap12";
    public static final String JMS_PREFIX = "jms";
    public static final String TNS_PREFIX = "tns";

    public static final String BINDING = "Binding";
    public static final String SOAP_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    public static final String SOAP12_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap12/http";

    public static final String DOCUMENT = "document";
    public static final String RPC = "rpc";
    public static final String LITERAL = "literal";
    public static final String REPLACE_WITH_ACTUAL_URL = "REPLACE_WITH_ACTUAL_URL";

    public static final String NS_XMLNS = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_SOAP11_HTTP_BINDING = "http://schemas.xmlsoap.org/soap/http";

    public static final String ATTR_TRANSPORT = "transport";
    public static final String ATTR_LOCATION = "location";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TNS = "targetNamespace";

    public static final QName QNAME_SCHEMA = new QName(NS_XMLNS, "schema");

    public static final QName QNAME_BINDING = new QName(NS_WSDL, "binding");
    public static final QName QNAME_DEFINITIONS = new QName(NS_WSDL, "definitions");
    public static final QName QNAME_DOCUMENTATION = new QName(NS_WSDL, "documentation");
    public static final QName NS_SOAP_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/",
                                                                  "address");
    public static final QName NS_XMLHTTP_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/http/",
                                                                     "address");
    public static final QName NS_SOAP_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding");
    public static final QName NS_SOAP_OPERATION = new QName("http://schemas.xmlsoap.org/wsdl/soap/",
                                                            "operation");
    public static final QName NS_SOAP_BODY = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body");
    public static final QName NS_SOAP_FAULT = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "fault");

    public static final QName NS_SOAP12_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap12/",
                                                            "binding");
    public static final QName NS_SOAP12_BINDING_ADDRESS = new QName(
                                                                    "http://schemas.xmlsoap.org/wsdl/soap12/",
                                                                    "address");

    public static final QName QNAME_IMPORT = new QName(NS_WSDL, "import");
    public static final QName QNAME_MESSAGE = new QName(NS_WSDL, "message");
    public static final QName QNAME_PART = new QName(NS_WSDL, "part");
    public static final QName QNAME_OPERATION = new QName(NS_WSDL, "operation");
    public static final QName QNAME_INPUT = new QName(NS_WSDL, "input");
    public static final QName QNAME_OUTPUT = new QName(NS_WSDL, "output");

    public static final QName QNAME_PORT = new QName(NS_WSDL, "port");
    public static final QName QNAME_ADDRESS = new QName(NS_WSDL, "address");
    public static final QName QNAME_PORT_TYPE = new QName(NS_WSDL, "portType");
    public static final QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    public static final QName QNAME_SERVICE = new QName(NS_WSDL, "service");
    public static final QName QNAME_TYPES = new QName(NS_WSDL, "types");

    // WSDL Validation
    public static final String ATTR_PART_ELEMENT = "element";
    public static final String ATTR_PART_TYPE = "type";
    //For LineNumDOMParser getUserData(location)
    public static final String NODE_LOCATION = "location";

    public static final int DOC_WRAPPED = 1;
    public static final int DOC_BARE = 2;
    public static final int RPC_WRAPPED = 3;
    public static final int ERORR_STYLE_USE = -1;

}
