package org.objectweb.celtix.ws.policy;

import javax.xml.namespace.QName;

/**
 * Encapsulation of version specific WS-Policy constants.
 */
public class PolicyConstantsImpl implements PolicyConstants {
        
    public PolicyConstantsImpl() {
    }

    public String getNamespaceURI() {        
        return Names.WSP_NAMESPACE_NAME;
    }
    
    public String getWSUNamespaceURI() {
        return Names.WSU_NAMESPACE_NAME;
    }

    public QName getPolicyQName() {
        return Names.WSP_POLICY_QNAME; 
    }

    public QName getPolicyReferenceQName() {
        return Names.WSP_POLICY_REFERENCE_QNAME;
    }
    
    
}
