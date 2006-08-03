package org.objectweb.celtix.jaxws.support;

import javax.jws.WebService;

import org.objectweb.celtix.service.factory.AbstractServiceConfiguration;

public class JaxWsServiceConfiguration extends AbstractServiceConfiguration {

    @SuppressWarnings("unchecked")
    WebService getWebServiceAttribute() {
        return (WebService) getServiceFactory().getServiceClass().getAnnotation(WebService.class);
    }
    
    @Override
    public String getServiceName() {
        WebService ws = getWebServiceAttribute();
        if (ws != null) return ws.serviceName();
        
        return null;
    }

    @Override
    public String getServiceNamespace() {
        WebService ws = getWebServiceAttribute();
        if (ws != null) return ws.targetNamespace();
        
        return null;
    }
}
