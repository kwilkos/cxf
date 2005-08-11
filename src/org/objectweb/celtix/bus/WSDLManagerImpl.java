package org.objectweb.celtix.bus;

import java.net.URL;
import java.util.WeakHashMap;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.wsdl.WSDLManager;

/**
 * WSDLManagerImpl
 * @author dkulp
 *
 */
class WSDLManagerImpl implements WSDLManager {
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
}
