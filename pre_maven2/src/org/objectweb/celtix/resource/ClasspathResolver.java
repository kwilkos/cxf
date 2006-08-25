package org.objectweb.celtix.resource;


import java.io.InputStream;


/**
 * Resolve resources from the system class path.
 */
public class ClasspathResolver implements ResourceResolver {

    public Object resolve(String resourceName, Class<?> resourceType) { 
        return null;
    } 

    public InputStream getAsStream(String name) { 
        return ClassLoader.getSystemResourceAsStream(name);
    } 
}
