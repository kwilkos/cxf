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

package org.apache.cxf.binding.soap;

import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import static org.apache.cxf.message.Message.MIME_HEADERS;


/**
 * This interceptor is responsible for setting up the SOAP version
 * and header, so that this is available to any pre-protocol interceptors
 * that require these to be available.
 */
public class SoapPreProtocolOutInterceptor extends AbstractSoapInterceptor {

    private static final ResourceBundle BUNDLE =
        BundleUtils.getBundle(SoapPreProtocolOutInterceptor.class);

    public SoapPreProtocolOutInterceptor() {
        super();
        setPhase(Phase.PRE_PROTOCOL);
    }

    /**
     * Mediate a message dispatch.
     * 
     * @param message the current message
     * @throws Fault
     */
    public void handleMessage(SoapMessage message) throws Fault {
        ensureVersion(message);
        ensureSoapHeader(message);
        ensureMimeHeaders(message);
    }
    
    /**
     * Ensure the SOAP version is set for this message.
     * 
     * @param message the current message
     */
    private void ensureVersion(SoapMessage message) {
        SoapVersion soapVersion = message.getVersion();
        if (soapVersion == null
            && message.getExchange().getInMessage() instanceof SoapMessage) {
            soapVersion = ((SoapMessage)message.getExchange().getInMessage()).getVersion();
            message.setVersion(soapVersion);
        }
        
        if (soapVersion == null) {
            soapVersion = Soap11.getInstance();
            message.setVersion(soapVersion);
        }
    }

    /**
     * Ensure the SOAP header is set for this message.
     * 
     * @param message the current message
     */
    private void ensureSoapHeader(SoapMessage message) {
        if (message.getHeaders(Element.class) == null) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = builderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                Message msg = new Message("PARSER_EXC", BUNDLE);
                throw new SoapFault(msg, e, SoapFault.SENDER);
            }
            Document doc = builder.newDocument();
            SoapVersion v = message.getVersion();
            Element header = doc.createElementNS(v.getNamespace(),
                                                 v.getHeader().getLocalPart());
            header.setPrefix(v.getPrefix());
            message.setHeaders(Element.class, header);
        }
    }
    
    /**
     * Ensure the SOAP header is set for this message.
     * 
     * @param message the current message
     */
    private void ensureMimeHeaders(SoapMessage message) {
        if (message.get(MIME_HEADERS) == null) {
            message.put(MIME_HEADERS, new HashMap<String, List<String>>());
        }
    }
}
