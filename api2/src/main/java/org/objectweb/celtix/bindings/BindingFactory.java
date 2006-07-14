package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
// import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * A factory interface for creating client and server bindings. 
 */
public interface BindingFactory {
    /**
     * Initialize the factory with a Bus reference.
     *
     * @param bus The <code>Bus</code> for this BindingFactory.
     */
    void init(Bus bus);
    
    /**
     * Creates a Binding using the <code>EndpointReferenceType</code>.
     *
     * @param reference The EndpointReferenceType the binding will use.
     * @return Binding The newly created Binding.
     */
    Binding createBinding(/*EndpointReferenceType reference*/)
        throws WSDLException, IOException;

}
