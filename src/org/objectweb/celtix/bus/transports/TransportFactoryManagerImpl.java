package org.objectweb.celtix.bus.transports;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public final class TransportFactoryManagerImpl implements TransportFactoryManager {

    private final Map<String, TransportFactory> transportFactories;
    private final Bus bus;
      
    public TransportFactoryManagerImpl(Bus b) throws BusException {
        transportFactories = new ConcurrentHashMap<String, TransportFactory>();
        bus = b;
        
        /*
        loadTransportFactory("org.objectweb.celtix.bus.transports.http.HTTPTransportFactory",
                             "http://schemas.xmlsoap.org/wsdl/soap/",
                             "http://celtix.objectweb.org/transports/http/configuration");
        */
        Object obj = bus.getConfiguration().getObject("transportFactories");
        
        List<ClassNamespaceMappingType> factoryMappings = ((ClassNamespaceMappingListType)obj).getMap();
        for (ClassNamespaceMappingType mapping : factoryMappings) {
            String classname = mapping.getClassname();
            List<String> namespaceList = mapping.getNamespace();
            String[] namespaces = new String[namespaceList.size()];
            namespaceList.toArray(namespaces);
            loadTransportFactory(classname, namespaces);
        }
    }
    
    public void loadTransportFactory(String classname, String ... namespaces) throws BusException {
        try {
            Class<? extends TransportFactory> clazz =
                Class.forName(classname).asSubclass(TransportFactory.class);
            
            TransportFactory factory = clazz.newInstance();
            factory.init(bus);
            for (String namespace : namespaces) {
                registerTransportFactory(namespace, factory);
            }
        } catch (ClassNotFoundException e) {
            throw new BusException(e);
        } catch (InstantiationException e) {
            throw new BusException(e);
        } catch (IllegalAccessException e) {
            throw new BusException(e);
        }
    }
    

    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.TransportFactoryManager#registerTransportFactory(java.lang.String, 
     * org.objectweb.celtix.transports.TransportFactory)
     */
    public void registerTransportFactory(String namespace, TransportFactory factory) throws BusException {
        transportFactories.put(namespace, factory);
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.TransportFactoryManager#deregisterTransportFactory(java.lang.String)
     */
    public void deregisterTransportFactory(String namespace)
        throws BusException {
        transportFactories.remove(namespace);
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.TransportFactoryManager#TransportFactory(java.lang.String)
     */
    public TransportFactory getTransportFactory(String namespace) throws BusException {
        return transportFactories.get(namespace);
    }
    
    
}
