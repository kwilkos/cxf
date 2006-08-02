package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class TypeInfo extends AbstractPropertiesHolder {
    ServiceInfo service;
    Map<String, SchemaInfo> schemas = new ConcurrentHashMap<String, SchemaInfo>(4);
    
    public TypeInfo(ServiceInfo serv) {
        service = serv;
    }
    
    public ServiceInfo getService() {
        return service;
    }
    
    public SchemaInfo addSchema(String namespaceURI) {
        if (namespaceURI == null) {
            throw new NullPointerException("Namespace URI cannot be null.");
        } 
        if (schemas.containsKey(namespaceURI)) {
            throw new IllegalArgumentException("An schema with namespaceURI [" + namespaceURI
                                               + "] already exists in this service");
        }
        SchemaInfo schemaInfo = new SchemaInfo(this, namespaceURI);
        addSchema(schemaInfo);
        return schemaInfo;
    }

    
    public void addSchema(SchemaInfo schemaInfo) {
        schemas.put(schemaInfo.getNamespaceURI(), schemaInfo);
    }

    
    public SchemaInfo getSchema(String namespaceURI) {
        return schemas.get(namespaceURI);
    }

    
    public Collection<SchemaInfo> getSchemas() {
        return Collections.unmodifiableCollection(schemas.values());
    } 

}
