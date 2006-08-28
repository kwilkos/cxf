/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;



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
