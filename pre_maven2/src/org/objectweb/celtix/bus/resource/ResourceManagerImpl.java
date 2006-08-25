package org.objectweb.celtix.bus.resource;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.types.StringListType;
import org.objectweb.celtix.resource.ClassLoaderResolver;
import org.objectweb.celtix.resource.ClasspathResolver;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;


public class ResourceManagerImpl implements ResourceManager {

    
    private static final Logger LOG = LogUtils.getL7dLogger(ResourceManagerImpl.class);
    private static ResourceManager instance; 

    private final List<ResourceResolver> resolvers = new LinkedList<ResourceResolver>();


    public ResourceManagerImpl() { 
        initializeDefaultResolvers(); 
    } 


    public ResourceManagerImpl(Bus bus) throws BusException { 

        Configuration conf = bus.getConfiguration(); 
        assert null != conf;
        Object obj = conf.getObject("resourceResolvers");
        assert null != obj;
        
        try { 
            for (String className : ((StringListType)obj).getItem()) { 
                if (LOG.isLoggable(Level.FINE)) { 
                    LOG.finest("attempting to load resolver " + className);
                }
                Class<? extends ResourceResolver> clz = getClass().getClassLoader().loadClass(className)
                    .asSubclass(ResourceResolver.class);

                ResourceResolver rr = clz.newInstance();
                resolvers.add(rr);
            } 
        } catch (Exception ex) { 
            throw new BusException(ex);
        } 
    } 
    
    public final Object resolveResource(String name, Class<?> type) { 
        return findResource(name, type, false);
    } 


    public final InputStream getResourceAsStream(String name) { 
        return (InputStream)findResource(name, null, true);
    } 


    public final void addResourceResolver(ResourceResolver resolver) { 
        if (!resolvers.contains(resolver)) { 
            resolvers.add(0, resolver);
        }
    } 

    public final void removeResourceResolver(ResourceResolver resolver) { 
        if (resolvers.contains(resolver)) { 
            resolvers.remove(resolver);
        }
    } 


    public final List getResourceResolvers() { 
        return resolvers; 
    }

    
    private Object findResource(String name, Class<?> type, boolean asStream) { 
        if (LOG.isLoggable(Level.FINE)) { 
            LOG.fine("resolving resource <" + name + ">" + (asStream ? " as stream "  
                                                            : " type <" + type + ">"));
        }

        Object ret = null; 
        for (ResourceResolver rr : resolvers) { 
            if (asStream) { 
                ret = rr.getAsStream(name);
            } else { 
                ret = rr.resolve(name, type);
            }
            if (ret != null) { 
                break;
            }
        } 
        return ret;
    } 


    public static synchronized ResourceManager instance() { 
        if (instance == null) { 
            instance = new ResourceManagerImpl();
        } 
        return instance;
    }

    private void initializeDefaultResolvers() { 
        addResourceResolver(new ClasspathResolver());
        addResourceResolver(new ClassLoaderResolver(getClass().getClassLoader()));
    } 
}
