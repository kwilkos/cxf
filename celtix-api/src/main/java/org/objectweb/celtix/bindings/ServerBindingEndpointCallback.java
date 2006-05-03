package org.objectweb.celtix.bindings;


import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;

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
    ServerDataBindingCallback getDataBindingCallback(QName operationName,
                                                     ObjectMessageContext objContext,
                                                     DataBindingCallback.Mode mode);
    
    DataBindingCallback getFaultDataBindingCallback(ObjectMessageContext objContext);

    
    Map<QName, ? extends DataBindingCallback> getOperations();
    javax.jws.soap.SOAPBinding.Style getStyle();
    
    /**
     * Returns the ServiceMode used for the given Implementor
     * @return the endpoint's service mode
     */
    DataBindingCallback.Mode getServiceMode();
    
    /**
     * Returns the WebServiceProvider used for the given Endpoint
     * @return the endpoint's service mode
     */
    WebServiceProvider getWebServiceProvider();

    /**
     * Returns the Executor to use to dispatch request
     * @return Executor to use to dispatch request
     */
    Executor getExecutor();
    
}
