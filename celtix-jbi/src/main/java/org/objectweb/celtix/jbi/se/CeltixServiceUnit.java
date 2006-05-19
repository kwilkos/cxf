package org.objectweb.celtix.jbi.se;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.component.ComponentContext;
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
    
    public void stop() {
        if (endpoint != null) {
            endpoint.stop();
        } else {
            serviceConsumer.stop();
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
                endpoint = new EndpointImpl(bus, serviceImplementation, null);
                // dummy endpoint to publish on
                endpoint.publish("http://foo/bar/baz");
            } else {
                LOG.info("starting consumer");
                classes = finder.findServiceConsumerClasses();
                Class<?> clz = classes.iterator().next();
                LOG.fine("starting consumer: " + clz);
                serviceConsumer = (ServiceConsumer)clz.newInstance();
                serviceConsumer.setComponentContext(ctx);
                new Thread(serviceConsumer).start();
            }
        } catch (Exception ex) { 
            if (ex.getCause() != null) { 
                ex = (Exception)ex.getCause();
            } 
            LOG.log(Level.SEVERE, "failed to publish endpoint", ex);
            // TODO throw decent exception here
            //throw new RuntimeException(ex);
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
    
    
    
    static class WebServiceClassFinder { 
        private final String rootPath;
        private final ClassLoader parent; 
        
        public WebServiceClassFinder(String argRootPath, ClassLoader loader) { 
            if (argRootPath.endsWith(File.separator)) { 
                argRootPath = argRootPath.substring(0, argRootPath.length() - 2);
            } 
            rootPath = argRootPath;
            parent = loader;
        } 
        
        public Collection<Class<?>> findServiceConsumerClasses() throws MalformedURLException { 
            return find(new Matcher() {
                public boolean accept(Class<?> clz) { 
                    return ServiceConsumer.class.isAssignableFrom(clz)
                        && (clz.getModifiers() & Modifier.ABSTRACT) == 0;
                }
            });
        } 
        
        public Collection<Class<?>> findWebServiceClasses() throws MalformedURLException { 
            
            return find(new Matcher() {
                public boolean accept(Class<?> clz) { 
                    return clz.getAnnotation(WebService.class) != null
                        && (clz.getModifiers() & Modifier.ABSTRACT) == 0;
                }
            });
        } 
        
        private Collection<Class<?>> find(Matcher matcher) throws MalformedURLException { 
            List<Class<?>> classes = new ArrayList<Class<?>>();
            
            File root = new File(rootPath);
            URL[] urls = {root.toURL()};
            URLClassLoader loader = new URLClassLoader(urls, parent);
            
            find(root, loader, classes, matcher);
            return classes;
        } 
        
        private void find(File dir, ClassLoader loader, Collection<Class<?>> classes, 
                          Matcher matcher) { 
            
            File[] files = dir.listFiles();
            for (File f : files) { 
                if (f.toString().endsWith(".class")) {
                    Class<?> clz = loadClass(loader, f);
                    if (matcher.accept(clz)) { 
                        classes.add(clz);
                    }
                } else if (f.isDirectory()) { 
                    find(f, loader, classes, matcher);
                } 
            } 
        } 
        
        
        private Class<?> loadClass(ClassLoader loader, File classFile) { 
            
            String fileName = classFile.toString();
            String className = fileName.substring(rootPath.length());
            className = className.substring(0, className.length() - ".class".length())
                .replace(File.separatorChar, '.');
            
            try { 
                return loader.loadClass(className);
            } catch (ClassNotFoundException ex) { 
                LOG.severe("failed to load class: " + className);
            } 
            return null;
        } 
        
        interface Matcher { 
            boolean accept(Class<?> clz);
        } 
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
            endpointName = providesEl.getAttribute("endpoint-name");
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
