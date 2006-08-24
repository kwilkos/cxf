package org.apache.cxf.bus.resource;

import java.util.Map;

import org.apache.cxf.BusException;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.PropertiesResolver;


public class ResourceManagerImpl extends DefaultResourceManager {

    public ResourceManagerImpl() {
        super();
    }
    
    public ResourceManagerImpl(Map<String, Object> properties) throws BusException { 
        super();
        registeredResolvers.clear();
        
        registeredResolvers.add(new PropertiesResolver(properties));
        
        // TODO: replace by dynamic loading
        
        /*
        Configuration conf = bus.getConfiguration(); g
        assert null != conf;
        Object obj = conf.getObject("resourceResolvers");
        assert null != obj;
        
        
        
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
