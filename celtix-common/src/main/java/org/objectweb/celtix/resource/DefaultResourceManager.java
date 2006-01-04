package org.objectweb.celtix.resource;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;

public class DefaultResourceManager implements ResourceManager {
    
    private static final Logger LOG = LogUtils.getL7dLogger(DefaultResourceManager.class);
    private static ResourceManager instance; 

    protected final List<ResourceResolver> resolvers = new LinkedList<ResourceResolver>();


    public DefaultResourceManager() { 
        initializeDefaultResolvers(); 
    } 
    
    public final <T> T resolveResource(String name, Class<T> type) { 
        return findResource(name, type, false);
    } 


    public final InputStream getResourceAsStream(String name) { 
        return findResource(name, InputStream.class, true);
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

    
    @SuppressWarnings("unchecked")
    private <T> T findResource(String name, Class<T> type, boolean asStream) {
        if (LOG.isLoggable(Level.FINE)) { 
            LOG.fine("resolving resource <" + name + ">" + (asStream ? " as stream "  
                                                            : " type <" + type + ">"));
        }

        T ret = null; 
        for (ResourceResolver rr : resolvers) { 
            if (asStream) { 
                ret = (T)rr.getAsStream(name);
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
