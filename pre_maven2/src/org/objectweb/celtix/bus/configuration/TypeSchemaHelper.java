package org.objectweb.celtix.bus.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeSchemaHelper {
    
    private static Map<String, TypeSchema> map = new HashMap<String, TypeSchema>();
    
    public TypeSchema get(String namespaceURI, String location) {
        TypeSchema ts = map.get(namespaceURI);
        if (null == ts) {
            ts = new TypeSchema(namespaceURI, location);
            map.put(namespaceURI, ts);
        }
        return ts;
    }
    
    public TypeSchema get(String namespaceURI) {
        return map.get(namespaceURI);
    }
    
    public Collection<TypeSchema> getTypeSchemas() {
        return map.values();
    }
    
    public void put(String namespaceURI, TypeSchema ts) {
        map.put(namespaceURI, ts);
    }
}
