package org.apache.cxf.resource;

import java.io.InputStream;

public class SinglePropertyResolver implements ResourceResolver {
   
    private final String key;
    private final Object value;
 
    public SinglePropertyResolver(String k, Object v) {
        key = k;
        value = v;
    }

    public InputStream getAsStream(String name) {
        return null;
    }

    public <T> T resolve(String resourceName, Class<T> resourceType) {
        if (null != value && key.equals(resourceName)) {
            return resourceType.cast(value);
        }
        return null;
    }    
}
