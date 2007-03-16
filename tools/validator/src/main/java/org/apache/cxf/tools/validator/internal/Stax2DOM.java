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

package org.apache.cxf.tools.validator.internal;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLConstants;

public class Stax2DOM {

    private  Element currentElement;
    private  Document doc;
   

    public Document getDocument(XMLEventReader xmlEventReader) throws ToolException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            throw new ToolException(e1);
        }
        doc = builder.newDocument();

        Element ele = doc.createElement("definitions");
        doc.appendChild(ele);
        currentElement = ele;

        while (xmlEventReader.hasNext()) {
            XMLEvent xmleve = (XMLEvent)xmlEventReader.next();

            if (xmleve.getEventType() == XMLStreamConstants.END_ELEMENT) {

                endElement();

            }

            if (xmleve.getEventType() == XMLStreamConstants.START_ELEMENT) {
                StartElement element = (StartElement)xmleve;
                startElement(element);

            }

        }

        return doc;
    }

    public void startElement(StartElement ele) {
        
        Element element = doc.createElementNS(ele.getName().getNamespaceURI(), ele.getName().getLocalPart());
        Iterator ite = ele.getAttributes();

        while (ite.hasNext()) {
            Attribute attr = (Attribute)ite.next();
            element.setAttribute(attr.getName().getLocalPart(), attr.getValue());
        }
        
        currentElement.appendChild(element);
        currentElement = element;
        element.setUserData(WSDLConstants.NODE_LOCATION, ele.getLocation(), null);

    }

    public void endElement() {
        currentElement = (Element)currentElement.getParentNode();
    }

}
