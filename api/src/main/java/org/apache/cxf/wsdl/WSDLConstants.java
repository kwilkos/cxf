/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.wsdl;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

public final class WSDLConstants {

    public static final String WSDL_PREFIX = "wsdl";
    public static final String WSDL11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";

    public static final String NP_XMLNS = "xmlns";
    public static final String NU_XMLNS = "http://www.w3.org/2000/xmlns/";

    // XML Schema (CR) datatypes + structures
    public static final String NP_SCHEMA_XSD = "xsd";
    public static final String NU_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";

    public static final QName SCHEMA_QNAME = new QName(NU_SCHEMA_XSD, "schema");

    // XML Schema instance
    public static final String NP_SCHEMA_XSI = "xsi";
    public static final String NU_SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    public static final String A_XSI_TYPE = "type";
    public static final String A_XSI_NIL = "nil";
    
    // XML Schema attribute names
    public static final QName NA_XSI_TYPE = new QName(NP_SCHEMA_XSI, A_XSI_TYPE, NU_SCHEMA_XSI);
    public static final QName NA_XSI_NIL = new QName(NP_SCHEMA_XSI, A_XSI_NIL, NU_SCHEMA_XSI);



    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String SOAP11_PREFIX = "soap";
    public static final String SOAP12_PREFIX = "soap12";
    
    public static final Map<String, String> NS_PREFIX_PAIR = new HashMap<String, String>(2);
    static {
        NS_PREFIX_PAIR.put(SOAP11_NAMESPACE, SOAP11_PREFIX);
        NS_PREFIX_PAIR.put(SOAP12_NAMESPACE, SOAP12_PREFIX);
    }

    public static final String NS_SOAP11_HTTP_BINDING = "http://schemas.xmlsoap.org/soap/http";
    
    public static final QName NS_SOAP_BINDING = new QName(SOAP11_NAMESPACE, "binding");
    public static final QName NS_SOAP_OPERATION = new QName(SOAP11_NAMESPACE, "operation");
    public static final QName NS_SOAP_BODY = new QName(SOAP11_NAMESPACE, "body");
    public static final QName NS_SOAP_FAULT = new QName(SOAP11_NAMESPACE, "fault");
    public static final QName NS_SOAP_BINDING_ADDRESS = new QName(SOAP11_NAMESPACE, "address");


    public static final String SOAP12_HTTP_TRANSPORT = "http://www.w3.org/2003/05/soap/bindings/HTTP/";
    
    public static final QName NS_SOAP12_BINDING = new QName(SOAP12_NAMESPACE, "binding");
    public static final QName NS_SOAP12_BINDING_ADDRESS = new QName(SOAP12_NAMESPACE, "address");
    

    public static final String DOCUMENT = "document";
    public static final String RPC = "rpc";
    public static final String LITERAL = "literal";
    public static final String REPLACE_WITH_ACTUAL_URL = "REPLACE_WITH_ACTUAL_URL";

    public static final String JMS_PREFIX = "jms";
    public static final String TNS_PREFIX = "tns";

    // WSDL 1.1 definitions
    public static final QName QNAME_BINDING = new QName(WSDL11_NAMESPACE, "binding");
    public static final QName QNAME_DEFINITIONS = new QName(WSDL11_NAMESPACE, "definitions");
    public static final QName QNAME_DOCUMENTATION = new QName(WSDL11_NAMESPACE, "documentation");
    public static final QName QNAME_IMPORT = new QName(WSDL11_NAMESPACE, "import");
    public static final QName QNAME_MESSAGE = new QName(WSDL11_NAMESPACE, "message");
    public static final QName QNAME_PART = new QName(WSDL11_NAMESPACE, "part");
    public static final QName QNAME_OPERATION = new QName(WSDL11_NAMESPACE, "operation");
    public static final QName QNAME_INPUT = new QName(WSDL11_NAMESPACE, "input");
    public static final QName QNAME_OUTPUT = new QName(WSDL11_NAMESPACE, "output");

    public static final QName QNAME_PORT = new QName(WSDL11_NAMESPACE, "port");
    public static final QName QNAME_ADDRESS = new QName(WSDL11_NAMESPACE, "address");
    public static final QName QNAME_PORT_TYPE = new QName(WSDL11_NAMESPACE, "portType");
    public static final QName QNAME_FAULT = new QName(WSDL11_NAMESPACE, "fault");
    public static final QName QNAME_SERVICE = new QName(WSDL11_NAMESPACE, "service");
    public static final QName QNAME_TYPES = new QName(WSDL11_NAMESPACE, "types");

    // WSDL Validation
    public static final String ATTR_PART_ELEMENT = "element";
    public static final String ATTR_PART_TYPE = "type";
    public static final String ATTR_TYPE = "type";

    //For Stax2DOM getUserData(location)
    public static final String NODE_LOCATION = "location";

    public static final int DOC_WRAPPED = 1;
    public static final int DOC_BARE = 2;
    public static final int RPC_WRAPPED = 3;
    public static final int ERORR_STYLE_USE = -1;

    public static final String XML_BINDING_NS = "http://cxf.apache.org/bindings/xformat";
    public static final QName NS_XMLHTTP_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/http/",
                                                                     "address");
    
    public static final String ATTR_TRANSPORT = "transport";
    public static final String ATTR_LOCATION = "location";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TNS = "targetNamespace";
    // usual prefix for the targetNamespace.
    public static final String CONVENTIONAL_TNS_PREFIX = "tns";
    
    public static final String WSDL11 = "1.1";
    public static final String WSDL20 = "2.0";

    public enum WSDLVersion {
        WSDL11,
        WSDL20,
        UNKNOWN
    };

    private WSDLConstants() {
    }
    
    public static WSDLVersion getVersion(String version) {
        if (WSDL11.equals(version)) {
            return WSDLVersion.WSDL11;
        }
        if (WSDL20.equals(version)) {
            return WSDLVersion.WSDL20;
        }
        return WSDLVersion.UNKNOWN;
    }
    
}
