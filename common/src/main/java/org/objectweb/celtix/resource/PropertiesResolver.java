package org.objectweb.celtix.resource;

import java.io.InputStream;
import java.util.Map;

public class PropertiesResolver implements ResourceResolver {
    
    private final Map<String, Object> properties; 
    
    public PropertiesResolver(Map<String, Object> p) {
        properties = p;
    }

    public InputStream getAsStream(String name) {
        return null;
    }

    public <T> T resolve(String resourceName, Class<T> resourceType) {
        return resourceType.cast(properties.get(resourceName));
    }    
}
