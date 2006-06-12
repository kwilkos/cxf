package org.objectweb.celtix.bus.ws.addressing;


import org.objectweb.celtix.ws.addressing.AddressingBuilder;
import org.objectweb.celtix.ws.addressing.AddressingConstants;
import org.objectweb.celtix.ws.addressing.AddressingProperties;


/**
 * Factory for WS-Addressing elements.
 * <p>
 * Note that the JAXB generated types are used directly to represent 
 * WS-Addressing schema types. Hence there are no factory methods defined
 * on this class for those types, as they may be instanted in the normal
 * way via the JAXB generated ObjectFactory.
 */
public class AddressingBuilderImpl extends AddressingBuilder {

    public AddressingBuilderImpl() {
    }

    //--AddressingType implementation

    /**
     * @return WS-Addressing namespace URI
     */
    public String getNamespaceURI() {
        return Names.WSA_NAMESPACE_NAME;
    }

    //--AddresingBuilder implementation

    /**
     * AddressingProperties factory method.
     *  
     * @return a new AddressingProperties instance
     */
    public AddressingProperties newAddressingProperties() {
        return new AddressingPropertiesImpl();
    }
    
    /**
     * AddressingConstants factory method.
     * 
     * @return an AddressingConstants instance
     */
    public AddressingConstants newAddressingConstants() {
        return new AddressingConstantsImpl();
    }
}
