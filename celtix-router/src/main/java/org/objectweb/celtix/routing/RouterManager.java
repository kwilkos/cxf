package org.objectweb.celtix.routing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.routing.configuration.UrlListPolicy;
import org.objectweb.celtix.tools.WSDLToJava;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolRunner;

public class RouterManager {
    public static final String ROUTING_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/routing/configuration";
    public static final String ROUTING_CONFIGURATION_ID = 
        "router";
    public static final String ROUTING_WSDL_ID =
        "routesWSDL";
    private final Bus bus;
    private final Configuration config;
    private URLClassLoader seiClassLoader;
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
    
    private URLClassLoader createSEIClassLoader(File classDir) {
        
        URLClassLoader loader = null;
        try {
            loader = URLClassLoader.newInstance(new URL[]{classDir.toURL()},
                                                Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException mue) {
            throw new WebServiceException("URLClassLoader creation failed", mue);
        }
        
        return loader;
    }
    
    private void loadWSDL() {
        try {
            List<String> wsdlUrlList = getRouteWSDLList();
            for (String wsdlUrl : wsdlUrlList) {
                URL url = getClass().getResource(wsdlUrl);
                //String url = getFile(wsdlUrl);
                wsdlModelList.add(bus.getWSDLManager().getDefinition(url));
            }
        } catch (WSDLException we) {
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
    
    protected void invokeWSDLToJava(File srcDir, File classDir) {
        List<String> wsdlUrlList = getRouteWSDLList();
       
        for (String wsdlUrl : wsdlUrlList) {
            invokeWSDLToJava(wsdlUrl, srcDir, classDir);
        }
    }

    private void invokeWSDLToJava(String wsdlUrl, File srcDir, File classDir) {
        try {        
            String file = getFile(wsdlUrl);
            if (null != file) {
                String[] args = new String[]{"-compile", 
                                             "-d", srcDir.getCanonicalPath(),
                                             "-classdir", classDir.getCanonicalPath(),
                                             file};
    
                ToolRunner.runTool(WSDLToJava.class,
                                   WSDLToJava.class.getResourceAsStream(ToolConstants.TOOLSPECS_BASE
                                                                        + "wsdl2java.xml"),
                                   false,
                                   args);
            }
        } catch (Exception ex) {
            throw new WebServiceException("wsdl2java exception", ex);
        }        
    }
    
    private String getFile(String wsdlUrl) {
        try {
            URL url = getClass().getResource(wsdlUrl);
            File f = new File(url.getFile());
            if (f.exists()) {
                wsdlUrl = f.getCanonicalPath();
            }
        } catch (IOException ioe) {
            throw new WebServiceException("Could not load wsdl", ioe);
        }
        return wsdlUrl;
    }
    
    private void mkDir(File dir) {
        if (dir == null) {
            throw new WebServiceException("Could not create dir");
        }
        
        if (dir.isFile()) {
            throw new WebServiceException("Unable to create directory as a file "
                                            + "already exists with that name: "
                                            + dir.getAbsolutePath());
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public void init() {
        factory = new RouterFactory(this);
        factory.init(bus);
        
        loadWSDL();
        
        File opDir = new File(System.getProperty("user.dir"), "/celtix-router-tmp");
        File classDir = new File(opDir, "/classes");
        mkDir(classDir);
        
        invokeWSDLToJava(opDir, classDir);
        seiClassLoader = createSEIClassLoader(classDir);
        
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

    public URLClassLoader getSEIClassLoader() {
        return seiClassLoader;
    }
    
    public static void main(String[] args) {
        try {
            Bus bus = Bus.init(args);
            RouterManager rm = new RouterManager(bus);
            rm.init();
            bus.run();
        } catch (BusException be) {
            throw new WebServiceException("Could not initialize bus", be);
        }
    }
}
