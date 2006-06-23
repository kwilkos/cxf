package org.objectweb.celtix.configuration.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeSchemaHelper {
    
    private static Map<String, TypeSchema> map = new HashMap<String, TypeSchema>();
    
    private final boolean forceDefaults;
    
    public TypeSchemaHelper(boolean fd) {
        forceDefaults = fd;
    }
    
    public static void clearCache() {
        map.clear();
    }
    
    public TypeSchema get(String namespaceURI, String base, String location) {
        TypeSchema ts = map.get(namespaceURI);
        if (null == ts) {
            ts = new TypeSchema(namespaceURI, base, location, forceDefaults);
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
