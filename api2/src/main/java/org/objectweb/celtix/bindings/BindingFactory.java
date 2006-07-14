package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
// import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.servicemodel.BindingInfo;
import org.objectweb.celtix.servicemodel.ServiceInfo;

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
    
    /**
     * Creates a BindingInfo object for collecting the metadata for the binding
     * 
     * May return null in which case a generic BindingInfo object will be used.
     * 
     * @param ns
     * @return
     */
    BindingInfo createBindingInfo(ServiceInfo parent);

}
