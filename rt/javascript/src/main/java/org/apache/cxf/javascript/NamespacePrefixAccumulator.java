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

package org.apache.cxf.javascript;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.service.model.SchemaInfo;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class NamespacePrefixAccumulator {
    private StringBuffer attributes;
    private Set<String> prefixes;
    private Map<String, String> fallbackNamespacePrefixMap;
    private int nsCounter;
    private SchemaInfo schemaInfo;

    public NamespacePrefixAccumulator(SchemaInfo schemaInfo) {
        attributes = new StringBuffer();
        prefixes = new HashSet<String>();
        fallbackNamespacePrefixMap = new HashMap<String, String>();
        nsCounter = 0;
        this.schemaInfo = schemaInfo;
    }
    
    public void collect(String prefix, String uri) {
        if (!prefixes.contains(prefix)) {
            attributes.append("xmlns:" + prefix + "='" + uri + "' ");
            prefixes.add(prefix);
        }
    }
    
    public String getAttributes() {
        return attributes.toString();
    }
    
    private String getPrefix(String namespaceURI) {
        String schemaPrefix = schemaInfo.getSchema().getNamespaceContext().getPrefix(namespaceURI);
        if (schemaPrefix == null || "tns".equals(schemaPrefix)) {
            schemaPrefix = fallbackNamespacePrefixMap.get(namespaceURI);
            if (schemaPrefix == null) {
                schemaPrefix = "jns" + nsCounter;
                nsCounter++;
                fallbackNamespacePrefixMap.put(namespaceURI, schemaPrefix);
            }
        }
        return schemaPrefix;
    }
    
    /**
     * This function obtains a name, perhaps namespace-qualified, for an element.
     * It also maintains a Map that records all the prefixes used in the course
     * of working on a single serializer function (and thus a single complex-type-element
     * XML element) which is used for namespace prefix management.
     * @param element
     * @param namespaceMap
     * @return
     */
    public String xmlElementString(XmlSchemaElement element) {
        String namespaceURI = XmlSchemaUtils.getElementQualifier(schemaInfo, element);
        if ("".equals(namespaceURI)) {
            return element.getName(); // use the non-qualified name.
        } else {
            // What if there were a prefix in the element's qname? This is not apparently 
            // something that happens in this environment.
            String prefix = getPrefix(namespaceURI);
            collect(prefix, namespaceURI);
            return prefix + ":" + element.getName();
        }
    }

}