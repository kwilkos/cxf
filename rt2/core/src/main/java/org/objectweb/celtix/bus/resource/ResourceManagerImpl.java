package org.objectweb.celtix.bus.resource;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.resource.DefaultResourceManager;


public class ResourceManagerImpl extends DefaultResourceManager {

    public ResourceManagerImpl() {
        super();
    }
    
    public ResourceManagerImpl(Bus bus) throws BusException { 
        super();
        registeredResolvers.clear();
        
        Configuration conf = bus.getConfiguration(); 
        assert null != conf;
        Object obj = conf.getObject("resourceResolvers");
        assert null != obj;
        
        // TODO: replace by dynamic loading
        
        /*
        try { 
            for (String className : ((StringListType)obj).getItem()) { 
                if (LOG.isLoggable(Level.FINEST)) { 
                    LOG.finest("attempting to load resolver " + className);
                }
                
                Class<? extends ResourceResolver> clz = getClass().getClassLoader().loadClass(className)
                    .asSubclass(ResourceResolver.class);

                ResourceResolver rr = clz.newInstance();
                registeredResolvers.add(rr);
            } 
        } catch (Exception ex) { 
            throw new BusException(ex);
        } 
        */
    } 
    
}
