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

package org.apache.cxf.databinding.source;

import java.util.Collection;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;

public class AbstractDataBinding {
    private Collection<DOMSource> schemas;

    public Collection<DOMSource> getSchemas() {
        return schemas;
    }

    public void setSchemas(Collection<DOMSource> schemas) {
        this.schemas = schemas;
    }
    

    protected XmlSchema addSchemaDocument(ServiceInfo serviceInfo, 
                                   XmlSchemaCollection col,
                                   Document d,
                                   String systemId) {
        String ns = d.getDocumentElement().getAttribute("targetNamespace");
        if (StringUtils.isEmpty(ns)) {
            ns = serviceInfo.getInterface().getName().getNamespaceURI();
            d.getDocumentElement().setAttribute("targetNamespace", ns);
        }
                           
        NodeList nodes = d.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element) {
                Element e = (Element) n;
                if (e.getLocalName().equals("import")) {
                    d.getDocumentElement().removeChild(e);
                }
            }
        }
        
        SchemaInfo schema = new SchemaInfo(serviceInfo, ns);
        schema.setElement(d.getDocumentElement());
        schema.setSystemId(systemId);
        XmlSchema xmlSchema = col.read(d.getDocumentElement());
        schema.setSchema(xmlSchema);
        serviceInfo.addSchema(schema);
        return xmlSchema;
    }
}
