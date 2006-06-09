package org.objectweb.celtix.bus.ws.addressing;

import org.objectweb.celtix.endpoints.ContextInspector;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


public class EndpointReferenceTypeContextInspector implements ContextInspector {
    
    public String getAddress(Object serverContext) {
        if (serverContext.getClass().isAssignableFrom(EndpointReferenceType.class)) {
            return ((EndpointReferenceType)serverContext).getAddress().getValue();
        } else {
            return null;
        }
    }

}
