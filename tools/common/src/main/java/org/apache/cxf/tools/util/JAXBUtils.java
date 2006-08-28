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

package org.apache.cxf.tools.util;

import org.w3c.dom.*;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ToolConstants;

public final class JAXBUtils {
    private JAXBUtils() {
    }
    
    private static Node innerJaxbBinding(Element schema) {
        String schemaNamespace = schema.getNamespaceURI();
        Document doc = schema.getOwnerDocument();
        
        Element annotation = doc.createElementNS(schemaNamespace, "annotation");
        Element appinfo  = doc.createElementNS(schemaNamespace, "appinfo");
        annotation.appendChild(appinfo);
        Element jaxbBindings = doc.createElementNS(ToolConstants.NS_JAXB_BINDINGS, "schemaBindings");
        appinfo.appendChild(jaxbBindings);
        return jaxbBindings;
    }

    public static Node innerJaxbPackageBinding(Element schema, String packagevalue) {
        Document doc = schema.getOwnerDocument();
        XMLUtils xmlUtils = new XMLUtils();
        if (!xmlUtils.hasAttribute(schema, ToolConstants.NS_JAXB_BINDINGS)) {
            schema.setAttributeNS(ToolConstants.NS_JAXB_BINDINGS, "version", "2.0");
        }

        Node schemaBindings = innerJaxbBinding(schema);
        
        Element packagename = doc.createElementNS(ToolConstants.NS_JAXB_BINDINGS, "package");
        packagename.setAttribute("name", packagevalue);
        
        schemaBindings.appendChild(packagename);

        return schemaBindings.getParentNode().getParentNode();
    }
}
