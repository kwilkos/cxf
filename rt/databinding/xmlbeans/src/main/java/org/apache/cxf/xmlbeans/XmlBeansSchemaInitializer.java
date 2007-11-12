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

package org.apache.cxf.xmlbeans;


import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;

/**
 * Walks the service model and sets up the element/type names.
 */
class XmlBeansSchemaInitializer extends ServiceModelVisitor {
    private static final Logger LOG = LogUtils.getLogger(XmlBeansSchemaInitializer.class);
    private SchemaCollection schemas;
    private XmlBeansDataBinding dataBinding;
    private Map<String, XmlSchema> schemaMap 
        = new HashMap<String, XmlSchema>();
    
    public XmlBeansSchemaInitializer(ServiceInfo serviceInfo,
                                     SchemaCollection col,
                                     XmlBeansDataBinding db) {
        super(serviceInfo);
        schemas = col;
        dataBinding = db;
    }
    

    XmlSchema getSchema(SchemaTypeSystem sts, String file) {
        if (schemaMap.containsKey(file)) {
            return schemaMap.get(file);
        }
        InputStream ins = sts.getSourceAsStream(file);
        try {
            Document doc = XMLUtils.parse(ins);
            XmlSchema schema = dataBinding.addSchemaDocument(serviceInfo,
                                                             schemas, 
                                                             doc, 
                                                             file);
            schemaMap.put(file, schema);
            return schema;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find schema for: " + file, e);
        }
    }

    @Override
    public void begin(MessagePartInfo part) {
        LOG.finest(part.getName().toString());
        // Check to see if the WSDL information has been filled in for us.
        if (part.getTypeQName() != null || part.getElementQName() != null) {
            checkForExistence(part);
            return;
        }
        
        Class<?> clazz = part.getTypeClass();
        if (clazz == null) {
            return;
        }

        boolean isFromWrapper = part.getMessageInfo().getOperation().isUnwrapped();
        if (isFromWrapper && clazz.isArray() && !Byte.TYPE.equals(clazz.getComponentType())) {
            clazz = clazz.getComponentType();
        }
        try {
            Field field = clazz.getField("type");
            SchemaType st = (SchemaType)field.get(null);
            
            SchemaTypeSystem sts = st.getTypeSystem();
            XmlSchema schema = getSchema(sts, st.getSourceName());

            if (st.getComponentType() == SchemaType.ELEMENT) {
                XmlSchemaElement sct = schema.getElementByName(st.getName());
                part.setXmlSchema(sct);
                part.setElement(true);
            } else {
                XmlSchemaType sct = schema.getTypeByName(st.getName());
                part.setTypeQName(st.getName());
                part.setXmlSchema(sct);
                part.setElement(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    public void checkForExistence(MessagePartInfo part) {
        QName qn = part.getElementQName();
        if (qn != null) {
            XmlSchemaElement el = schemas.getElementByQName(qn);
            if (el == null) {
                Class<?> clazz = part.getTypeClass();
                if (clazz == null) {
                    return;
                }

                boolean isFromWrapper = part.getMessageInfo().getOperation().isUnwrapped();
                if (isFromWrapper && clazz.isArray() && !Byte.TYPE.equals(clazz.getComponentType())) {
                    clazz = clazz.getComponentType();
                }
                
                //FIXME - find and set the part.setXmlSchema(....) info
            }
        }
    }
    
}
