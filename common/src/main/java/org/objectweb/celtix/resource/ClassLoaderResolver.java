package org.apache.cxf.resource;

import java.io.InputStream;
import java.net.URL;

public class ClassLoaderResolver implements ResourceResolver {

    private final ClassLoader loader; 

    public ClassLoaderResolver() { 
        this(ClassLoaderResolver.class.getClassLoader());
    }

    public ClassLoaderResolver(ClassLoader l) { 
        loader = l;
    }
 
    public <T> T resolve(String resourceName, Class<T> resourceType) {
        if (resourceType == null) {
            return null;
        }
        URL url = loader.getResource(resourceName);
        if (resourceType.isInstance(url)) {
            return resourceType.cast(url);
        }
        return null;
    } 

    public InputStream getAsStream(String name) { 
        return loader.getResourceAsStream(name);
    } 

}
