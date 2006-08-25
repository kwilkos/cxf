package org.objectweb.celtix.ws.addressing;

import org.objectweb.celtix.endpoints.ContextInspector;


public class EndpointReferenceTypeContextInspector implements ContextInspector {
    
    public String getAddress(Object serverContext) {
        if (serverContext.getClass().isAssignableFrom(EndpointReferenceType.class)) {
            return ((EndpointReferenceType)serverContext).getAddress().getValue();
        } else {
            return null;
        }
    }

}
