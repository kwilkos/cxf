package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;



public class TypeInfo extends AbstractPropertiesHolder {
    private static final Logger LOG = LogUtils.getL7dLogger(TypeInfo.class);
    
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
            throw new NullPointerException(new Message("NAMESPACE.URI.NOT.NULL", LOG).toString());
        } 
        if (schemas.containsKey(namespaceURI)) {
            throw new IllegalArgumentException(
                new Message("DUPLICATED.NAMESPACE", LOG, new Object[]{namespaceURI}).toString());
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
