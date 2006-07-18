package org.objectweb.celtix.endpoint;

import org.objectweb.celtix.servicemodel.EndpointInfo;

public interface EndpointFactory {

    Endpoint createEndpoint(EndpointInfo info, String bindingURI);
}
