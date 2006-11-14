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

package demo.ws_addressing.common;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import org.apache.cxf.ws.addressing.AddressingBuilder;
import org.apache.cxf.ws.addressing.AddressingConstants;


/**
 * Snoops SOAP headers.
 */
public class HeaderSnooper implements SOAPHandler<SOAPMessageContext> {

    private static final AddressingConstants ADDRESSING_CONSTANTS = 
        AddressingBuilder.getAddressingBuilder().newAddressingConstants();
    private static final String WSA_NAMESPACE_URI = 
        ADDRESSING_CONSTANTS.getNamespaceURI();

    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        snoop(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        snoop(context);
        return true;
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }

    private void snoop(SOAPMessageContext context) {
        try {
            SOAPHeader header = 
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            if (header != null) {
                System.out.println(getDirection(context)
                                   + " WS-Addressing headers");
                Iterator headerElements = header.examineAllHeaderElements();
                while (headerElements.hasNext()) {
                    SOAPHeaderElement headerElement =
                        (SOAPHeaderElement)headerElements.next();
                    Name headerName = headerElement.getElementName();
                    if (WSA_NAMESPACE_URI.equals(headerName.getURI())) {
                        System.out.println(headerName.getLocalName()
                                           + getText(headerElement));
                    }
                }
                System.out.println();
            }
        } catch (SOAPException se) {
            System.out.println("SOAP header snoop failed: " + se);
        }
    }

    private String getDirection(SOAPMessageContext context) {
        Boolean outbound = (Boolean)context.get(MESSAGE_OUTBOUND_PROPERTY);
        return outbound != null && outbound.booleanValue()
               ? "Outbound"
               : "Inbound";
    }

    private String getText(SOAPHeaderElement headerElement) {
        String text = " : ";
        Iterator children = headerElement.getChildElements();
        if (children.hasNext()) {
            text += ((Node)children.next()).getValue();            
        }
        return text;
    }

}

