package org.objectweb.celtix.bindings;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.ServiceMode;

import org.objectweb.celtix.context.ObjectMessageContext;

/**
 * ServerBindingEndpointCallback
 * Callback used from ServerBinding's to create the DataBindingCallback object used during dispatch
 * to handle the IO requirements of the endpoint. 
 */
public interface ServerBindingEndpointCallback {
    
    /**
     * Creates the DataBindingCallback object
     * @param objContext The ObjectMessageContext for the dispatch
     * @param mode The Mode for the dispatch
     * @return a DataBinding callback object
     */
    DataBindingCallback createDataBindingCallback(ObjectMessageContext objContext,
                                                  DataBindingCallback.Mode mode);

    /**
     * Returns the method in the <code>Endpoint</code>'s implementor that
     * implements the specified operation. 
     * 
     * @param endpoint
     * @param operationName
     * @return the <code>Method</code> in the <code>Endpoint</code>'s implementor.
     */
    Method getMethod(Endpoint endpoint, QName operationName);
    
    
    /**
     * Returns the ServiceMode used for the given Endpoint
     * @param endpoint
     * @return the endpoint's service mode
     */
    ServiceMode getServiceMode(Endpoint endpoint);
}
