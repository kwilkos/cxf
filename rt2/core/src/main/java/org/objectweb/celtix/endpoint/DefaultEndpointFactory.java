package org.objectweb.celtix.endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.servicemodel.EndpointInfo;

public class DefaultEndpointFactory implements EndpointFactory {

    public Endpoint createEndpoint(EndpointInfo info, String bindingURI) {
        return new EndpointImpl(Bus.getCurrent(), info, bindingURI);
    }

}
