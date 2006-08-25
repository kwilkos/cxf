package org.objectweb.celtix.ws.policy;

import javax.xml.namespace.QName;

public class Names {

    public static final String WSP_NAMESPACE_NAME = 
        "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    public static final String WSU_NAMESPACE_NAME = 
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    
    public static final QName WSP_POLICY_QNAME = 
        new QName(WSP_NAMESPACE_NAME, "Policy");
    
    public static final QName WSP_POLICY_REFERENCE_QNAME = 
        new QName(WSP_NAMESPACE_NAME, "PolicyReference");
    
}
