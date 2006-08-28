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

package org.apache.cxf.tools.common.extensions.jms;

import java.io.*;
import java.lang.reflect.*;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ToolConstants;

public class JMSAddressSerializer implements ExtensionSerializer,
                                             ExtensionDeserializer,
                                             Serializable {
    public static final long serialVersionUID = 1;
    XMLUtils xmlUtils = new XMLUtils();
    
    public void marshall(Class parentType,
                         QName elementType,
                         ExtensibilityElement extension,
                         PrintWriter pw,
                         Definition def,
                         ExtensionRegistry extReg) throws WSDLException {

        JMSAddress jmsAddress = (JMSAddress)extension;
        StringBuffer sb = new StringBuffer(300);        
        sb.append(" <" + xmlUtils.writeQName(def, elementType) + " ");
        sb.append(jmsAddress.getAttrXMLString());
        sb.append("/>");
        pw.print(sb.toString());
        pw.println();
    }

    public ExtensibilityElement unmarshall(Class parentType,
                                           QName elementType,
                                           Element el,
                                           Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {

        JMSAddress jmsAddress = (JMSAddress)extReg.createExtension(parentType, elementType);
        jmsAddress.setElementType(elementType);
        jmsAddress.setElement(el);
        jmsAddress.setDocumentBaseURI(def.getDocumentBaseURI());
        
        JMSAddressParser parser = new JMSAddressParser();
        parser.parseElement(jmsAddress, el);
        
        if (jmsAddress.getAddress() == null || jmsAddress.getAddress().trim().length() == 0) {
            if (def.getNamespaces() != null) {
                jmsAddress.setAddress((String)def.getNamespaces().get("jms"));
            } else {
                jmsAddress.setAddress(ToolConstants.NS_JMS_ADDRESS);
            }
        }
        return jmsAddress;
    }
}
