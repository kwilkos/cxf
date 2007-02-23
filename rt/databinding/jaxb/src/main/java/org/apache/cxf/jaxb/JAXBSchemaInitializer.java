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

package org.apache.cxf.jaxb;

import java.util.Iterator;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;

import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.ws.commons.schema.XmlSchemaCollection;

/**
 * Walks the service model and sets up the element/type names.
 */
class JAXBSchemaInitializer extends ServiceModelVisitor {

    private XmlSchemaCollection schemas;
    private JAXBContextImpl context;
    
    public JAXBSchemaInitializer(ServiceInfo serviceInfo, XmlSchemaCollection col, JAXBContextImpl context) {
        super(serviceInfo);
        schemas = col;
        this.context = context;
    }

    @Override
    public void begin(MessagePartInfo part) {
        // Check to see if the WSDL information has been filled in for us.
        if (part.getTypeQName() != null || part.getElementQName() != null) {
            return;
        }
        
        Class<?> clazz = part.getTypeClass();
        if (clazz == null) {
            return;
        }

        JaxBeanInfo<?> beanInfo = context.getBeanInfo(clazz);

        boolean isElement = beanInfo.isElement();
        part.setElement(isElement);
        if (isElement) {
            QName name = new QName(beanInfo.getElementNamespaceURI(null), 
                                   beanInfo.getElementLocalName(null));
            part.setElementQName(name);
            part.setXmlSchema(schemas.getElementByQName(name));
        } else {
            Iterator<QName> itr = beanInfo.getTypeNames().iterator();
            if (!itr.hasNext()) {
                return;
            }
            
            QName typeName = itr.next();

            part.setTypeQName(typeName);
            part.setXmlSchema(schemas.getTypeByQName(typeName));
        }
    }
}
