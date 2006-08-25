package org.objectweb.celtix.bindings;

import java.lang.reflect.Method;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
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
     * Returns a collection of class objects that are annotated with WebService annotation.
     * @return <code>List</code> of <code>Class</code> objects.
     */
    List<Class<?>> getWebServiceAnnotatedClass();    
}
