package org.objectweb.celtix.resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;

public class DefaultResourceManager implements ResourceManager {
    
    private static final Logger LOG = LogUtils.getL7dLogger(DefaultResourceManager.class);
    private static ResourceManager instance; 

    protected final List<ResourceResolver> registeredResolvers = new LinkedList<ResourceResolver>();


    public DefaultResourceManager() { 
        initializeDefaultResolvers(); 
    } 
    
    public final <T> T resolveResource(String name, Class<T> type) { 
        return findResource(name, type, false, registeredResolvers);
    } 

    public final <T> T resolveResource(String name, Class<T> type, List<ResourceResolver> resolvers) { 
        return findResource(name, type, false, resolvers);
    } 

    
    public final InputStream getResourceAsStream(String name) { 
        return findResource(name, InputStream.class, true, registeredResolvers);
    } 


    public final void addResourceResolver(ResourceResolver resolver) { 
        if (!registeredResolvers.contains(resolver)) { 
            registeredResolvers.add(0, resolver);
        }
    } 

    public final void removeResourceResolver(ResourceResolver resolver) { 
        if (registeredResolvers.contains(resolver)) { 
            registeredResolvers.remove(resolver);
        }
    } 


    public final List<ResourceResolver> getResourceResolvers() {
        List<ResourceResolver> ret = new ArrayList<ResourceResolver>();
        ret.addAll(registeredResolvers);
        return ret; 
    }

    
    private <T> T findResource(String name, Class<T> type, boolean asStream, 
                               List<ResourceResolver> resolvers) {
        
        if (LOG.isLoggable(Level.FINE)) { 
            LOG.fine("resolving resource <" + name + ">" + (asStream ? " as stream "  
                                                            : " type <" + type + ">"));
        }

        T ret = null; 
        
        for (ResourceResolver rr : resolvers) { 
            if (asStream) { 
                ret = type.cast(rr.getAsStream(name));
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
            instance = new DefaultResourceManager();
        } 
        return instance;
    }
    public static synchronized void clearInstance() {
        instance = null;
    }

    private void initializeDefaultResolvers() { 
        addResourceResolver(new ClasspathResolver());
        addResourceResolver(new ClassLoaderResolver(getClass().getClassLoader()));
    } 

}
