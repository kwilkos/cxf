package org.objectweb.celtix.jbi.se;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.EndpointUtils;
import org.objectweb.celtix.jbi.ServiceConsumer;

/**
 * Wraps a Celtix service or client.
 */
public class CeltixServiceUnit {
    
    private static final Logger LOG = Logger.getLogger(CeltixServiceUnit.class.getName());
    
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
        
        URL url = null; 
        try { 
            url = new File(path + File.separator).toURL();
            
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "failed to initialize service unit", ex);
        } 
        bus = b;
        rootPath = path;
        parent.addResource(url);
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
                LOG.severe("failed to deactive Endpoint" + ref + e);
            }
        } else {
            serviceConsumer.stop();
        }
    }
    
    public void start(ComponentContext ctx, CeltixServiceUnitManager serviceUnitManager) {
        if (isServiceProvider()) { 
            LOG.info("starting provider");
            ref = null;
            try {
                ref = ctx.activateEndpoint(getServiceName(), getEndpointName());
            } catch (JBIException e) {
                LOG.severe("failed to active Endpoint" + e);
            } 
            LOG.fine("activated endpoint: " + ref.getEndpointName() 
                     + " service: " + ref.getServiceName());
            serviceUnitManager.putServiceEndpoint(ref, this);
            
        } else {
            LOG.info("starting consumer");
            new Thread(serviceConsumer).start();
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
                LOG.info("publishing endpoint");
                isProvider = true;
                Class<?> clz = classes.iterator().next();
                serviceImplementation = clz.newInstance();
                LOG.info("the class name is " + serviceImplementation.getClass());
                if (EndpointUtils.isValidImplementor(serviceImplementation)) {
                    endpoint = new EndpointImpl(bus, serviceImplementation, null);
                    //dummy endpoint to publish on
                    endpoint.publish("http://foo/bar/baz");
                }
                
            } else {
                classes = finder.findServiceConsumerClasses();
                Class<?> clz = classes.iterator().next();
                serviceConsumer = (ServiceConsumer)clz.newInstance();
                serviceConsumer.setComponentContext(ctx);
                
            }
        } catch (Exception ex) { 
                      
            if (ex.getCause() != null) { 
                ex = (Exception)ex.getCause();
            } 
   
            LOG.log(Level.SEVERE, "failed to publish endpoint", ex);
        } 
    } 
    
    
    public ClassLoader getClassLoader() { 
        return parentLoader;
    } 
    
    
    Document getWsdlAsDocument() { 
        
        Document doc = null;
        try { 
            WebService ws = serviceImplementation.getClass().getAnnotation(WebService.class);
            if (ws != null) { 
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.parse(ws.wsdlLocation());
            } else { 
                LOG.severe("could not get WebService annotation from " + serviceImplementation);
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
            if (providesEl != null) {
                endpointName = providesEl.getAttribute("endpoint-name");
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
    
    
}
