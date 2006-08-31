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

package org.apache.cxf.binding.soap.interceptor;


import java.util.ResourceBundle;



import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.common.i18n.BundleUtils;

import org.apache.cxf.phase.Phase;

import org.apache.cxf.staxutils.StaxUtils;

public class SoapOutInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SoapOutInterceptor.class);

    public SoapOutInterceptor() {
        super();
        setPhase(Phase.WRITE);
    }
    
    public void handleMessage(SoapMessage message) {
        try {
            XMLStreamWriter xtw = message.getContent(XMLStreamWriter.class);
            message.setContent(XMLStreamWriter.class, xtw);
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
            
            xtw.writeStartElement(soapVersion.getPrefix(), 
                                  soapVersion.getEnvelope().getLocalPart(),
                                  soapVersion.getNamespace());
            xtw.writeNamespace(soapVersion.getPrefix(), soapVersion.getNamespace());
            
            //handleHeaderPart(message);

            Element eleHeaders = message.getHeaders(Element.class);
            

            if (eleHeaders != null) {
                StaxUtils.writeElement(eleHeaders, xtw, true);
            }
            
            xtw.writeStartElement(soapVersion.getPrefix(), 
                                  soapVersion.getBody().getLocalPart(),
                                  soapVersion.getNamespace());
            
            // Calling for Wrapped/RPC/Doc/ Interceptor for writing SOAP body
            message.getInterceptorChain().doIntercept(message);

            xtw.writeEndElement();
            
            // Write Envelop end element
            xtw.writeEndElement();
            
            xtw.flush();
        } catch (XMLStreamException e) {
            //e.printStackTrace();
            throw new SoapFault(
                new org.apache.cxf.common.i18n.Message("XML_WRITE_EXC", BUNDLE), e, SoapFault.SENDER);
        }
    }
    
    /*private void handleHeaderPart(SoapMessage message) {
        //add MessagePart to soapHeader if necessary
        Exchange exchange = message.getExchange();
        BindingOperationInfo operation = (BindingOperationInfo)exchange.get(BindingOperationInfo.class
                                                                            .getName());
        Element soapHeaders = message.getHeaders(Element.class);
         
         
         
        int countParts = 0;
        List<MessagePartInfo> parts = null;
        if (!isRequestor(message)) {
            parts = operation.getOutput().getMessageInfo().getMessageParts();
        } else {
            parts = operation.getInput().getMessageInfo().getMessageParts();
        }
        countParts = parts.size();
 
        if (countParts > 0) {
            List<?> objs = message.getContent(List.class);
            Object[] args = objs.toArray();
            Object[] els = parts.toArray();
 
            if (args.length != els.length) {
                message.setContent(Exception.class,
                                   new RuntimeException("The number of arguments is not equal!"));
            }
 
            for (int idx = 0; idx < countParts; idx++) {
                //Object arg = args[idx];
                MessagePartInfo part = (MessagePartInfo)els[idx];
                if (!part.isInSoapHeader()) {
                    //this part should be in header, so write to header
                    continue;
                }
                 
                // todo write to header
                if (soapHeaders == null) {
                    DocumentBuilder builder = null;
                    try {
                        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }
                    Document doc = builder.newDocument();
                    SoapVersion version = message.getVersion();
                    soapHeaders =
                        doc.createElementNS(version.getNamespace(), version.getHeader().getLocalPart());
                    QName headerPartQName = ServiceModelUtil.getPartName(part);
                    soapHeaders.appendChild(
                        doc.createElementNS(
                            headerPartQName.getNamespaceURI(), headerPartQName.getLocalPart()));
                }
                 
            }
        }
        message.setHeaders(Element.class, soapHeaders);

    }       
    
    protected boolean isRequestor(Message message) {
        return Boolean.TRUE.equals(message.containsKey(Message.REQUESTOR_ROLE));
    }*/

}
