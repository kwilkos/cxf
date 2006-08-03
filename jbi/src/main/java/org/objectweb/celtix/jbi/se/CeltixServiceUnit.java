package org.objectweb.celtix.jbi.se;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.WebServiceClient;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.EndpointUtils;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.jbi.ServiceConsumer;

/**
 * Wraps a Celtix service or client.
 */
public class CeltixServiceUnit {
    
    private static final Logger LOG = LogUtils.getL7dLogger(CeltixServiceUnit.class);
    
    private final Bus bus; 
    
    private Object serviceImplementation; 
    private ServiceConsumer serviceConsumer; 
    private EndpointImpl endpoint;
    private final String rootPath; 
    private final ClassLoader parentLoader;
    private boolean isProvider;
    private QName serviceName; 
    private String endpointName;
    private ServiceEndpoint ref;
    
    public CeltixServiceUnit(Bus b, String path, ComponentClassLoader parent) {
        
        bus = b;
        rootPath = path;
        parentLoader = parent;
          
        parseJbiDescriptor(); 
    }
    
    public boolean isServiceProvider() { 
        return isProvider;
    } 
    
    public void stop(ComponentContext ctx) {
        if (ref != null) {
            try {
                ctx.deactivateEndpoint(ref);
            } catch (JBIException e) {
                LOG.severe(new Message("SU.FAILED.DEACTIVE.ENDPOINT", LOG).toString() 
                           + ref + e);
            }
        } else {
            serviceConsumer.stop();
        }
    }
    
    public void start(ComponentContext ctx, CeltixServiceUnitManager serviceUnitManager) {
        ClassLoader oldLoader = null;
        if (isServiceProvider()) { 
            LOG.fine(new Message("SU.START.PROVIDER", LOG).toString());
            oldLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(serviceImplementation.getClass().getClassLoader());
            ref = null;
            try {
                ref = ctx.activateEndpoint(getServiceName(), getEndpointName());
            } catch (JBIException e) {
                LOG.severe(new Message("SU.FAIED.ACTIVE.ENDPOINT", LOG).toString() + e);
            } 
            LOG.fine("activated endpoint: " + ref.getEndpointName() 
                     + " service: " + ref.getServiceName());
            serviceUnitManager.putServiceEndpoint(ref, this);
            Thread.currentThread().setContextClassLoader(oldLoader);
        } else {
            LOG.fine(new Message("SU.START.CONSUMER", LOG).toString());
            oldLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(serviceConsumer.getClass().getClassLoader());
            new Thread(serviceConsumer).start();
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }
    
    public QName getServiceName() { 
        
        QName ret = null; 
        
        if (isServiceProvider()) { 
            if (serviceName == null) { 
                WebService ws = (WebService)serviceImplementation.getClass().getAnnotation(WebService.class);
                serviceName = new QName(ws.targetNamespace(), ws.serviceName());
            }
            ret = serviceName;
        } else {
            WebServiceClient ws;
            WebServiceClassFinder finder = new WebServiceClassFinder(rootPath, parentLoader);
            Collection<Class<?>> classes = null;
            try {
                classes = finder.findWebServiceClient();
            } catch (MalformedURLException e) {
                LOG.severe("Exception Caught:" + e);
            } 
            if (classes.size() > 0) {
                Class<?> clz = classes.iterator().next();
                ws = clz.getAnnotation(WebServiceClient.class);
                serviceName = new QName(ws.targetNamespace(), ws.name());
                ret = serviceName;
            }
        }
        return ret;
    } 
    
    public String getEndpointName() { 
        return endpointName;
    } 
    
    public void prepare(ComponentContext ctx) { 
        
        try { 
            
            WebServiceClassFinder finder = new WebServiceClassFinder(rootPath, parentLoader);
            Collection<Class<?>> classes = finder.findWebServiceClasses(); 
            if (classes.size() > 0) {
                LOG.fine(new Message("SU.PUBLISH.ENDPOINT", LOG).toString());
                isProvider = true;
                Class<?> clz = classes.iterator().next();
                serviceImplementation = clz.newInstance();
                if (EndpointUtils.isValidImplementor(serviceImplementation)) {
                    createProviderConfiguration();
                    ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(
                        serviceImplementation.getClass().getClassLoader());
                    endpoint = new EndpointImpl(bus, serviceImplementation, null);
                    //dummy endpoint to publish on
                    endpoint.publish("http://foo/bar/baz");
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
                
            } else {
                createConsumerConfiguration();
                classes = finder.findServiceConsumerClasses();
                Class<?> clz = classes.iterator().next();
                serviceConsumer = (ServiceConsumer)clz.newInstance();
                serviceConsumer.setComponentContext(ctx);
                
            }
        } catch (Exception ex) { 
                      
            if (ex.getCause() != null) { 
                ex = (Exception)ex.getCause();
            } 
   
            LOG.log(Level.SEVERE, new Message("SU.FAILED.PUBLISH.ENDPOINT", LOG).toString(), ex);
        } 
    } 
    
    
    public ClassLoader getClassLoader() { 
        return parentLoader;
    } 
    
    
    Document getWsdlAsDocument() { 
        
        Document doc = null;
        try { 
            WebService ws = null;
            WebServiceClassFinder finder = new WebServiceClassFinder(rootPath, parentLoader);
            Collection<Class<?>> classes = finder.findWebServiceInterface(); 
            if (classes.size() > 0) {
                Class<?> clz = classes.iterator().next();
                ws = clz.getAnnotation(WebService.class);
            }
            if (ws != null) { 
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.parse(ws.wsdlLocation());
                
            } else { 
                LOG.severe(new Message("SU.COULDNOT.GET.ANNOTATION", LOG).toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        return doc;
    } 
    
    
    
    
    
    private void parseJbiDescriptor() { 
        
        // right now, all we are interested in is an endpoint name
        // from the jbi dd.
        File metaInf = new File(rootPath, "META-INF");
        File jbiXml = new File(metaInf, "jbi.xml");
        try { 
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(jbiXml.toURL().toString());
            
            Element providesEl = (Element)findNode(doc.getDocumentElement(), "provides");
            Element consumersEl = (Element)findNode(doc.getDocumentElement(), "consumes");
            if (providesEl != null) {
                endpointName = providesEl.getAttribute("endpoint-name");
            } else if (consumersEl != null) {
                endpointName = consumersEl.getAttribute("endpoint-name");
            }
        } catch (Exception ex) { 
            LOG.log(Level.SEVERE, "error parsing " + jbiXml, ex);
        } 
        
    } 
    
    
    private Node findNode(Node root, String name) { 
        
        if (name.equals(root.getNodeName())) {
            return root;
        } 
        
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) { 
            Node child = children.item(i);
            Node found = findNode(child, name);
            if (found != null) { 
                return found;
            } 
        } 
        return null;
    } 
    
    private void createProviderConfiguration() {
        String oldConfiguration = System.getProperty("celtix.config.file");
        File metaInfDir = new File(rootPath, "META-INF");
        File celtixConfig = new File(metaInfDir, "celtix-server.xml"); 
        if (celtixConfig.exists()) { 
            try {
                System.setProperty("celtix.config.file", celtixConfig.toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            LOG.fine(new Message("SE.SET.CONFIGURATION", LOG) + System.getProperty("celtix.config.file"));
        } else { 
            LOG.severe(new Message("SE.NOT.FOUND.CONFIGURATION", LOG).toString() + metaInfDir);
        } 
        
        Configuration busCfg = bus.getConfiguration();
        if (null == busCfg) {
            return;
        }

        String id = getServiceName().toString();
        ConfigurationBuilder cb = bus.getConfigurationBuilder();
        cb.buildConfiguration(EndpointImpl.ENDPOINT_CONFIGURATION_URI, id, busCfg);
        System.setProperty("celtix.config.file", oldConfiguration);
    }
    
    private void createConsumerConfiguration() {
        String oldConfiguration = System.getProperty("celtix.config.file");
        File metaInfDir = new File(rootPath, "META-INF");
        File celtixConfig = new File(metaInfDir, "celtix-client.xml"); 
        if (celtixConfig.exists()) { 
            try {
                System.setProperty("celtix.config.file", celtixConfig.toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            LOG.fine(new Message("SE.SET.CONFIGURATION", LOG) + System.getProperty("celtix.config.file"));
        } else { 
            LOG.severe(new Message("SE.NOT.FOUND.CONFIGURATION", LOG).toString() + metaInfDir);
        } 
        
        Configuration busCfg = bus.getConfiguration();
        if (null == busCfg) {
            return;
        }
        String id = getServiceName().toString() + "/" + getEndpointName();
        LOG.info("the client bean id is " + id);
        ConfigurationBuilder cb = bus.getConfigurationBuilder();
        cb.buildConfiguration(ServiceImpl.PORT_CONFIGURATION_URI, id, busCfg);
        System.setProperty("celtix.config.file", oldConfiguration);
    }

    
}
