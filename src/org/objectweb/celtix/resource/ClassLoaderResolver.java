package org.objectweb.celtix.resource;

import java.io.InputStream;

public class ClassLoaderResolver implements ResourceResolver {

    private final ClassLoader loader; 

    public ClassLoaderResolver() { 
        this(ClassLoaderResolver.class.getClassLoader());
    }

    public ClassLoaderResolver(ClassLoader l) { 
        loader = l;
    }
 
    public Object resolve(String resourceName, Class<?> resourceType) { 
        return null;
    } 

    public InputStream getAsStream(String name) { 
        return loader.getResourceAsStream(name);
    } 

}
