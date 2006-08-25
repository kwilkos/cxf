package org.objectweb.celtix.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
//import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;

public class RouterFactory {
    private static final Logger LOG = LogUtils.getL7dLogger(RouterFactory.class);
    private RouterManager mgr;    
    private Bus bus;

    
    public RouterFactory(RouterManager rm) {
        mgr = rm;
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

    @SuppressWarnings("unchecked")
    public List<Router> addRoutes(Definition def) {
        List<Router> routerList = new ArrayList<Router>();
        List<ExtensibilityElement> extList = def.getExtensibilityElements();
        for (ExtensibilityElement extEl : extList) {
            if (extEl instanceof RouteType) {
                RouteType rt = (RouteType)extEl;
                if (isValidRoute(def, rt)) {
                    Router router = createRouter(def, rt);
                    router.init();
                    routerList.add(router);
                } else {
                    //throw new WebServiceException(
                    //            new Message("UNSUPPORTED_ROUTE", LOG, rt.getName()).toString());
                    if (LOG.isLoggable(Level.SEVERE)) {
                        LOG.log(Level.SEVERE, "UNSUPPORTED_ROUTE", rt.getName());
                    }
                }
            }
        }

        return routerList;
    }
    
    @SuppressWarnings("unchecked")
    private boolean isValidRoute(Definition model, RouteType route) {
        List<SourceType> source = route.getSource();
        List<DestinationType> dest = route.getDestination();
        if (source.size() != 1
            || dest.size() != 1
            || route.isSetMultiRoute()
            || route.isSetOperation()) {
            return false;
        }
        
        //Check For Different Bindings.
        SourceType st = source.get(0);
        DestinationType dt = dest.get(0);
        
        //Get Service Name
        Service sourceService = model.getService(st.getService());
        Service destService = model.getService(dt.getService());
        if (null == sourceService
            || null == destService) {
            return false;
        }
        
        Port sourcePort = sourceService.getPort(st.getPort());
        Port destPort = destService.getPort(dt.getPort());
        
        if (null == sourcePort
            || null == destPort) {
            return false;
        }

        return true;
    }
    
    public Router createRouter(Definition model, RouteType route) {
        return new RouterImpl(mgr.getSEIClassLoader(), model, route);
    }
}
