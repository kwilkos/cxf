package org.objectweb.celtix.routing;

import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensionRegistry;

import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;

public class RouterFactory {
    private Bus bus;
    
    public RouterFactory() {
        //Complete
    }
   
    public void init(Bus b) {
        bus = b;
        registerRouterExtension(bus.getWSDLManager().getExtenstionRegistry());
    }
    
    private void registerRouterExtension(ExtensionRegistry registry) {
        try {
            JAXBExtensionHelper.addExtensions(registry,
                                              Definition.class,
                                              RouteType.class);
        } catch (JAXBException e) {
            throw new WebServiceException("Adding of routeType extension failed.");
        }
    }

    public Router createRouter(RouteType route) {
        return null;
    }
}
