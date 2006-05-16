package org.objectweb.celtix.bus.resource;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.types.StringListType;
import org.objectweb.celtix.resource.DefaultResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;


public class ResourceManagerImpl extends DefaultResourceManager {
    
    private static final Logger LOG = LogUtils.getL7dLogger(ResourceManagerImpl.class);

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
    } 
    
}
