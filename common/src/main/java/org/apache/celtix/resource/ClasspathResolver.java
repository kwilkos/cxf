package org.apache.cxf.resource;


import java.io.InputStream;
import java.net.URL;

/**
 * Resolve resources from the system class path.
 */
public class ClasspathResolver implements ResourceResolver {

    public <T> T resolve(String resourceName, Class<T> resourceType) { 
        URL url = ClassLoader.getSystemResource(resourceName);
        if (resourceType.isInstance(url)) {
            return resourceType.cast(url);
        }
        return null;
    } 

    public InputStream getAsStream(String name) { 
        return ClassLoader.getSystemResourceAsStream(name);
    } 
}
