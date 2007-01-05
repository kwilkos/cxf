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

package org.apache.cxf.tools.common.extensions.xmlformat;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ToolConstants;

public class XMLFormatSerializer implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    
    
    public void marshall(Class parentType, QName elementType, ExtensibilityElement extension, PrintWriter pw,
                         Definition def, ExtensionRegistry extReg) throws WSDLException {

        XMLFormat xmlFormat = (XMLFormat)extension;
        StringBuffer sb = new StringBuffer(300);
        sb.append("<" + XMLUtils.writeQName(def, elementType) + " ");
        if (xmlFormat.getRootNode() != null) {
            sb.append(ToolConstants.XMLBINDING_ROOTNODE + "=\""
                      + XMLUtils.writeQName(def, xmlFormat.getRootNode()) + "\"");
        }
        sb.append(" />");
        pw.print(sb.toString());
        pw.println();
    }

    public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el, Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {

        XMLFormat xmlFormat = (XMLFormat)extReg.createExtension(parentType, elementType);
        xmlFormat.setElement(el);
        xmlFormat.setElementType(elementType);
        xmlFormat.setDocumentBaseURI(def.getDocumentBaseURI());
        XMLFormatParser xmlBindingParser = new XMLFormatParser();
        xmlBindingParser.parseElement(def, xmlFormat, el);
        return xmlFormat;
    }

}
