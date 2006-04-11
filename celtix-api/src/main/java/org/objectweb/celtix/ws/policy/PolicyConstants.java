package org.objectweb.celtix.ws.policy;

import javax.xml.namespace.QName;

/**
 * Encapsulation of version-specific WS-Policy constants.
 */
public interface PolicyConstants {
    /**
     * @return namespace defined by the WS-Policy schema
     */
    String getNamespaceURI();
    
    /**
     * @return namespace defined by the WS Security Utility schema
     */
    String getWSUNamespaceURI();
    
    /**
     * @return the QName of the Policy element
     */
    QName getPolicyQName();
    
    /**
     * @return the QName of the PolicyReference element
     */
    QName getPolicyReferenceQName();
    
}
