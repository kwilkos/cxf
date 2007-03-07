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
package org.apache.cxf.aegis.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains type mappings for java/qname pairs.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Feb 21, 2004
 */
public class CustomTypeMapping implements TypeMapping {
    private static final Log LOG = LogFactory.getLog(CustomTypeMapping.class);

    private Map<Class, Type> class2Type;

    private Map<QName, Type> xml2Type;

    private Map<Class, QName> class2xml;

    private TypeMapping defaultTM;

    private String encodingStyleURI;

    private TypeCreator typeCreator;

    public CustomTypeMapping(TypeMapping defaultTM) {
        this();

        this.defaultTM = defaultTM;
    }

    public CustomTypeMapping() {
        class2Type = Collections.synchronizedMap(new HashMap<Class, Type>());
        class2xml = Collections.synchronizedMap(new HashMap<Class, QName>());
        xml2Type = Collections.synchronizedMap(new HashMap<QName, Type>());
    }

    public boolean isRegistered(Class javaType) {
        boolean registered = class2Type.containsKey(javaType);

        if (!registered && defaultTM != null) {
            registered = defaultTM.isRegistered(javaType);
        }

        return registered;
    }

    public boolean isRegistered(QName xmlType) {
        boolean registered = xml2Type.containsKey(xmlType);

        if (!registered && defaultTM != null) {
            registered = defaultTM.isRegistered(xmlType);
        }

        return registered;
    }

    public void register(Class javaType, QName xmlType, Type type) {
        type.setSchemaType(xmlType);
        type.setTypeClass(javaType);

        register(type);
    }

    public void register(Type type) {
        type.setTypeMapping(this);
        /*
         * -- prb@codehaus.org; changing this to only register the type for
         * actions that it supports, and it could be none.
         */
        if (type.getTypeClass() != null) {
            class2xml.put(type.getTypeClass(), type.getSchemaType());
            class2Type.put(type.getTypeClass(), type);
        }
        if (type.getSchemaType() != null) {
            xml2Type.put(type.getSchemaType(), type);
        }
        if (type.getTypeClass() == null && type.getSchemaType() == null) {
            LOG
                .warn("The type "
                      + type.getClass().getName()
                      + " supports neither serialization (non-null TypeClass) nor deserialization (non-null SchemaType).");
        }
    }

    public void removeType(Type type) {
        if (!xml2Type.containsKey(type.getSchemaType())) {
            defaultTM.removeType(type);
        } else {
            xml2Type.remove(type.getSchemaType());
            class2Type.remove(type.getTypeClass());
            class2xml.remove(type.getTypeClass());
        }
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMapping#getType(java.lang.Class)
     */
    public Type getType(Class javaType) {
        Type type = class2Type.get(javaType);

        if (type == null && defaultTM != null) {
            type = defaultTM.getType(javaType);
        }

        return type;
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMapping#getType(javax.xml.namespace.QName)
     */
    public Type getType(QName xmlType) {
        Type type = xml2Type.get(xmlType);

        if (type == null && defaultTM != null) {
            type = defaultTM.getType(xmlType);
        }

        return type;
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMapping#getTypeQName(java.lang.Class)
     */
    public QName getTypeQName(Class clazz) {
        QName qname = class2xml.get(clazz);

        if (qname == null && defaultTM != null) {
            qname = defaultTM.getTypeQName(clazz);
        }

        return qname;
    }

    public String getEncodingStyleURI() {
        return encodingStyleURI;
    }

    public void setEncodingStyleURI(String encodingStyleURI) {
        this.encodingStyleURI = encodingStyleURI;
    }

    public TypeCreator getTypeCreator() {
        return typeCreator;
    }

    public void setTypeCreator(TypeCreator typeCreator) {
        this.typeCreator = typeCreator;

        typeCreator.setTypeMapping(this);
    }

    public TypeMapping getParent() {
        return defaultTM;
    }
}
