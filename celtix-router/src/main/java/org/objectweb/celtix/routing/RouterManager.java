package org.objectweb.celtix.routing;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.routing.configuration.UrlListPolicy;

public class RouterManager {
    public static final String ROUTING_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/routing/configuration";
    public static final String ROUTING_CONFIGURATION_ID = 
        "router";
    public static final String ROUTING_WSDL_ID =
        "routesWSDL";
    public static final String ROUTER_CONFIG_RESOURCE =
        "config-metadata/router-config.xml";
    private final Bus bus;
    private final Configuration config;
    private RouterFactory factory;
    private List<Definition> wsdlModelList;
    private List<Router> routerList;
    
    public RouterManager(Bus b) {
        bus = b;
        config = createConfiguration();
        wsdlModelList = new ArrayList<Definition>();
        routerList = new ArrayList<Router>();
    }

    private Configuration createConfiguration() {
        
        Configuration busCfg = bus.getConfiguration();
        assert null != busCfg;
        Configuration cfg = null;
        ConfigurationBuilder cb = ConfigurationBuilderFactory.getBuilder(null);
        cb.addModel(ROUTER_CONFIG_RESOURCE);
        cfg = cb.getConfiguration(ROUTING_CONFIGURATION_URI, 
                                  ROUTING_CONFIGURATION_ID,
                                  busCfg);
        if (null == cfg) {
            cfg = cb.buildConfiguration(ROUTING_CONFIGURATION_URI,
                                        ROUTING_CONFIGURATION_ID,
                                        busCfg);
        }
        return cfg;
    }
    
    private void loadWSDL() {
        try {
            List<String> wsdlUrlList = getRouteWSDLList();
            for (String wsdlUrl : wsdlUrlList) {
                URL url = getClass().getResource(wsdlUrl);
                wsdlModelList.add(bus.getWSDLManager().getDefinition(url));
            }
        } catch (Exception we) {
            throw new WebServiceException("Could not load router wsdl", we);
        }
    }

    private void addRoutes() {
        for (Definition def : wsdlModelList) {
            List<Router> rList = factory.addRoutes(def);
            routerList.addAll(rList);
        }
    }
    
    protected void publishRoutes() {
        for (Router r : routerList) {
            r.publish();
        }
    }
    
    public void init() {
        factory = new RouterFactory();
        factory.init(bus);
        loadWSDL();
        addRoutes();
        publishRoutes();
    }
    
    public List<String> getRouteWSDLList() {
        UrlListPolicy urlList = config.getObject(UrlListPolicy.class, ROUTING_WSDL_ID);
        if (null == urlList) {
            throw new WebServiceException("Router WSDL not specified");
        }
        return urlList.getUrl();
    }
    
    public RouterFactory getRouterFactory() {
        return factory;
    }

    public List<Router> getRouters() {
        return routerList;
    }
    
    public static void main(String[] args) {
        try {
            Bus bus = Bus.init(args);
            Bus.setCurrent(bus);
            RouterManager rm = new RouterManager(bus);
            rm.init();
            bus.run();
        } catch (BusException be) {
            throw new WebServiceException("Could not initialize bus", be);
        }
    }
}
