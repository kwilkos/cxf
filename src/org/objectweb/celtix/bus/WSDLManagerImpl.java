package org.objectweb.celtix.bus;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.w3c.dom.Element;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.tools.common.ToolConfig;
import org.objectweb.celtix.tools.common.generators.JAXWSWsdlGenerator;
import org.objectweb.celtix.wsdl.WSDLManager;

/**
 * WSDLManagerImpl
 * @author dkulp
 *
 */
class WSDLManagerImpl implements WSDLManager {
    
    private static Logger logger = Logger.getLogger(WSDLManagerImpl.class.getName());
    
    final ExtensionRegistry registry;
    final WSDLFactory factory;
    final WeakHashMap<Object, Definition> definitionsMap;
    
    
    WSDLManagerImpl(Bus bus) throws BusException {
        try {
            factory = WSDLFactory.newInstance();
            registry = factory.newPopulatedExtensionRegistry();            
        } catch (WSDLException e) {
            throw new BusException(e);
        }
        definitionsMap = new WeakHashMap<Object, Definition>();        
    }

    public WSDLFactory getWSDLFactory() {
        return factory;
    }
    /* (non-Javadoc)
     * @see org.objectweb.celtix.wsdl.WSDLManager#getExtenstionRegistry()
     */
    public ExtensionRegistry getExtenstionRegistry() {
        return registry;
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.wsdl.WSDLManager#getDefinition(java.net.URL)
     */
    public Definition getDefinition(URL url) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(url)) {
                return definitionsMap.get(url);
            }
        }
        Definition def = loadDefinition(url.toString());
        synchronized (definitionsMap) {
            definitionsMap.put(url, def);
        }
        return def;
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.wsdl.WSDLManager#getDefinition(java.net.URL)
     */
    public Definition getDefinition(String url) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(url)) {
                return definitionsMap.get(url);
            }
        }
        return loadDefinition(url);
    }
    

    public Definition getDefinition(Element el) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(el)) {
                return definitionsMap.get(el);
            }
        }
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(registry);
        Definition def = reader.readWSDL(null, el);
        synchronized (definitionsMap) {
            definitionsMap.put(el, def);
        }
        return def;
    }
    
    public Definition getDefinition(Class sei) throws WSDLException {
        synchronized (definitionsMap) {
            if (definitionsMap.containsKey(sei)) {
                return definitionsMap.get(sei);
            }
        }
       
        Definition def = createDefinition(sei);
        synchronized (definitionsMap) {
            definitionsMap.put(sei, def);
        }
        return def;
    }
    
    
    private Definition loadDefinition(String url) throws WSDLException {
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(registry);
        Definition def = reader.readWSDL(url);
        synchronized (definitionsMap) {
            definitionsMap.put(url, def);
        }
        return def;
    }
    
    private Definition createDefinition(Class sei) {          
        File tmp = null;
        try {
            tmp = File.createTempFile("tmp", ".wsdl");
            tmp.delete();
            tmp.mkdir();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not create tmp directory in which to generate WSDL.", ex);
            return null;
        }
        JAXWSWsdlGenerator generator = new JAXWSWsdlGenerator(sei.getName(), sei.getClassLoader());
        Configuration config = new ToolConfig(new String[] {"-wsdl", "-d", tmp.getPath()});
        generator.setConfiguration(config);
        generator.generate();

        // schema and WSDL file should have been created in tmp directory

        File[] generated = tmp.listFiles();
        File schema = null;
        File wsdl = null;
        for (File f : generated) {
            if (f.isFile()) {
                if (null == wsdl && f.getName().endsWith(".wsdl")) {
                    wsdl = f;
                } else if (null == schema && f.getName().endsWith(".xsd")) {
                    schema = f;
                }
                if (null != schema && null != wsdl) {
                    break;
                }
            }
        }
        if (null == wsdl || null == schema) {
            logger.severe("Wsdl and/or schema files could not be generated.");
            return null;
        }

        WSDLFactory wf = getWSDLFactory();
        Definition definition = null;
        
        class Directory {
            private File dir;
            Directory(File d) {
                dir = d;
            }
            void delete() {
                File[] entries = dir.listFiles();
                for (File f : entries) {
                    if (f.isDirectory()) {
                        Directory d = new Directory(f);
                        d.delete();
                    } else {
                        f.delete();
                    }
                }
            }
        }
        try {
            definition = wf.newWSDLReader().readWSDL(wsdl.getPath());
        } catch (WSDLException ex) {
            logger.log(Level.SEVERE, "Could not read generated wsdl.", ex);
        }
        
        Directory dir = new Directory(tmp);
        dir.delete();        
        
        return definition; 
    }
}
