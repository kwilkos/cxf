package org.objectweb.celtix.tools.common;

import javax.xml.namespace.QName;

public final class ToolConstants {

    public static final String TOOLSPECS_BASE = "/org/objectweb/celtix/tools/common/toolspec/toolspecs/";
    public static final String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String WSDL_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";
    
    public static final String DEFAULT_TEMP_DIR = "gen_tmp";
    public static final String CFG_OUTPUTDIR = "outputdir";
    public static final String CFG_OUTPUTFILE = "outputfile";
    public static final String CFG_WSDLURL = "wsdlurl";
    public static final String CFG_NAMESPACE = "namespace";
    public static final String CFG_VERBOSE = "verbose";
    public static final String CFG_PORT = "port";
    public static final String CFG_BINDING = "binding";    
    public static final String CFG_WEBSERVICE = "webservice";
    public static final String CFG_SERVER = "server";
    public static final String CFG_CLIENT = "client";
    public static final String CFG_ALL = "all";
    public static final String CFG_IMPL = "impl";
    public static final String CFG_PACKAGENAME = "packagename";
    public static final String CFG_NINCLUDE = "ninclude";
    public static final String CFG_NEXCLUDE = "nexclude";
    public static final String CFG_CMD_ARG = "args";
    public static final String CFG_INSTALL_DIR = "install.dir";
    public static final String CFG_PLATFORM_VERSION = "platform.version";
    public static final String CFG_COMPILE = "compile";
    public static final String CFG_CLASSDIR = "classdir";
    public static final String CFG_EXTRA_SOAPHEADER = "exsoapheader";
    public static final String CFG_DEFAULT_NS = "defaultns";
    public static final String CFG_DEFAULT_EX = "defaultex";
    
   
    // WSDL2Java Constants
    
    public static final String CFG_TYPES = "types";
    public static final String CFG_INTERFACE = "interface";
    public static final String CFG_NIGNOREEXCLUDE = "nignoreexclude";
    public static final String CFG_ANT = "ant";
    public static final String CFG_LIB_REF = "library.references";
    public static final String CFG_ANT_PROP = "ant.prop";
    
    // Java2WSDL Constants
    
    public static final String CFG_CLASSPATH = "classpath";
    public static final String CFG_TNS = "tns";
    public static final String CFG_SERVICENAME = "servicename";
    public static final String CFG_SCHEMANS = "schemans";
    public static final String CFG_USETYPES = "usetypes";
    public static final String CFG_CLASSNAME = "classname";
    public static final String CFG_PORTTYPE = "porttype";
 

    // WSDL2Service Constants
    public static final String CFG_ADDRESS = "address";
    public static final String CFG_TRANSPORT = "transport";
    public static final String CFG_SERVICE = "service";
    public static final String CFG_BINDING_ATTR = "attrbinding";
    
    // WSDL2Soap Constants    
    public static final String CFG_STYLE = "style";
    public static final String CFG_USE = "use";    
    
    // XSD2WSDL Constants
    public static final String CFG_XSDURL = "xsdurl";
    public static final String CFG_NAME = "name";
        
    // WSDL2Java Processor Constants
    public static final String SEI_GENERATOR = "sei.generator";
    public static final String FAULT_GENERATOR = "fault.generator";
    public static final String TYPE_GENERATOR = "type.generator";
    public static final String IMPL_GENERATOR = "impl.generator";
    public static final String SVR_GENERATOR = "svr.generator";
    public static final String CLT_GENERATOR = "clt.generator";
    public static final String SERVICE_GENERATOR = "service.generator";
    public static final String ANT_GENERATOR = "ant.generator";
    public static final String HANDLER_GENERATOR = "handler.generator";
    public static final String GENERATED_CLASS_COLLECTOR = "generatedClassCollector";

    // Binding namespace
    public static final String NS_JAXWS_BINDINGS = "http://java.sun.com/xml/ns/jaxws";
    public static final String NS_JAXB_BINDINGS = "http://java.sun.com/xml/ns/jaxb";
    public static final QName  JAXWS_BINDINGS = new QName(NS_JAXWS_BINDINGS, "bindings");
    public static final QName  JAXB_BINDINGS = new QName(NS_JAXB_BINDINGS, "bindings");
    public static final String JAXWS_BINDINGS_WSDL_LOCATION = "wsdlLocation";
    public static final String JAXWS_BINDING_NODE = "node";
    public static final String JAXWS_BINDING_VERSION = "version";

    public static final String ASYNC_METHOD_SUFFIX = "Async";
    
    public static final String HANDLER_CHAINS_URI = "http://java.sun.com/xml/ns/javaee";
    public static final String HANDLER_CHAIN = "handler-chain";
    public static final String HANDLER_CHAINS = "handler-chains";

    public static final String RAW_JAXB_MODEL = "rawjaxbmodel";
    
    // JMS adress
    public static final String NS_JMS_ADDRESS = "http://celtix.objectweb.org/transports/jms";
    public static final QName  JMS_ADDRESS = new QName(NS_JMS_ADDRESS, "address");

    public static final String JMS_ADDR_DEST_STYLE = "destinationStyle";
    public static final String JMS_ADDR_JNDI_URL = "jndiProviderURL";
    public static final String JMS_ADDR_JNDI_FAC = "jndiConnectionFactoryName";
    public static final String JMS_ADDR_JNDI_DEST = "jndiDestinationName";
    public static final String JMS_ADDR_MSG_TYPE = "messageType";
    public static final String JMS_ADDR_INIT_CTX = "initialContextFactory";
    public static final String JMS_ADDR_SUBSCRIBER_NAME = "durableSubscriberName";
    public static final String JMS_ADDR_MSGID_TO_CORRID = "useMessageIDAsCorrelationID";
    
    // XML Binding
    public static final String XMLBINDING_ROOTNODE = "rootNode";
    public static final String XMLBINDING_HTTP_LOCATION = "location";
    public static final String NS_XML_FORMAT = "http://celtix.objectweb.org/bindings/xmlformat";
    public static final String XML_FORMAT_PREFIX = "xformat";
    public static final String NS_XML_HTTP = "http://schemas.xmlsoap.org/wsdl/http/";
    public static final String XML_HTTP_PREFIX = "http";
    public static final QName  XML_HTTP_ADDRESS = new QName(NS_XML_HTTP, "address");
    public static final QName  XML_FORMAT = new QName(NS_XML_FORMAT, "body");
    public static final QName  XML_BINDING_FORMAT = new QName(NS_XML_FORMAT, "binding");
}
