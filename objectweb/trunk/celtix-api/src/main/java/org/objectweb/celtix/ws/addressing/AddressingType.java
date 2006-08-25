package org.objectweb.celtix.ws.addressing;


/**
 * Encapsulates the WS-Addressing namespace URI (and by implication, also the
 * version).
 */
public interface AddressingType {
    
    /**
     * @return WS-Addressing namespace URI
     */
    String getNamespaceURI();
}
