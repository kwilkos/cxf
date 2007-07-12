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

package org.apache.cxf.jaxws.interceptors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.w3c.dom.Node;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.wsdl.WSDLConstants;

public class DispatchOutDatabindingInterceptor extends AbstractOutDatabindingInterceptor {
    private static final Logger LOG = LogUtils.getL7dLogger(DispatchOutDatabindingInterceptor.class);
    private DispatchOutDatabindingEndingInterceptor ending;
    
    private Service.Mode mode;
    
    public DispatchOutDatabindingInterceptor(Mode mode) {
        super(Phase.WRITE);
        ending = new DispatchOutDatabindingEndingInterceptor();
        
        this.mode = mode;
    }

    public void handleMessage(Message message) throws Fault {
        org.apache.cxf.service.Service service = 
            message.getExchange().get(org.apache.cxf.service.Service.class);

        Object obj = null;
        Object result = message.getContent(List.class);
        if (result != null) {
            obj = ((List)result).get(0);
            message.setContent(Object.class, obj);
        } else {
            obj = message.getContent(Object.class);
        }

        if (obj == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("DISPATCH_OBJECT_CANNOT_BE_NULL", LOG));
        }

        if (message instanceof SoapMessage) {
            if (mode == Service.Mode.PAYLOAD) {
                if (obj instanceof SOAPMessage || obj instanceof DataSource) {
                    throw new Fault(
                                    new org.apache.cxf.common.i18n.Message(
                                        "DISPATCH_OBJECT_NOT_SUPPORTED_SOAPBINDING",
                                        LOG, obj.getClass(), "PAYLOAD"));
                } else {
                    // Input is Source or JAXB in payload mode, need to wrap it
                    // with a SOAPMessage
                    try {
                        SOAPMessage msg = initSOAPMessage();
                        DataWriter<Node> dataWriter = getDataWriter(message, service, Node.class);
                        dataWriter.write(obj, msg.getSOAPBody());
                        message.setContent(Object.class, msg);
                        message.setContent(SOAPMessage.class, msg);
                    } catch (SOAPException e) {
                        throw new Fault(new org.apache.cxf.common.i18n.Message("EXCEPTION_WRITING_OBJECT",
                                                                               LOG), e);
                    }
                }
            } else {
                if (obj instanceof DataSource) {
                    throw new Fault(
                                    new org.apache.cxf.common.i18n.Message(
                                        "DISPATCH_OBJECT_NOT_SUPPORTED_SOAPBINDING",
                                        LOG, "DataSource", "MESSAGE"));
                } else if (obj instanceof Source || obj instanceof SOAPMessage) {
                    // Input is Source or SOAPMessage, no conversion needed
                } else {
                    //REVISIT: JAXB element in Message mode, is this a valid input?
                }
            }
        } else if (message instanceof XMLMessage) {
            if (obj instanceof SOAPMessage) {
                throw new Fault(
                                new org.apache.cxf.common.i18n.Message(
                                    "DISPATCH_OBJECT_NOT_SUPPORTED_XMLBINDING",
                                    LOG, "SOAPMessage", "PAYLOAD/MESSAGE"));
            }

            if (mode == Service.Mode.PAYLOAD && obj instanceof DataSource) {
                throw new Fault(
                                new org.apache.cxf.common.i18n.Message(
                                    "DISPATCH_OBJECT_NOT_SUPPORTED_XMLBINDING",
                                    LOG, "DataSource", "PAYLOAD"));
            }

            if (obj instanceof Source || obj instanceof DataSource) {
                // no conversion needed
            } else {
                // JAXB element
                try {
                    DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message, service,
                                                                           XMLStreamWriter.class);
                    W3CDOMStreamWriter xmlWriter = new W3CDOMStreamWriter();
                    dataWriter.write(obj, xmlWriter);                    

                    Source source = new DOMSource(xmlWriter.getDocument().getDocumentElement());   
                    message.setContent(Object.class, source);                
                } catch (ParserConfigurationException e) {
                    throw new Fault(new org.apache.cxf.common.i18n.Message("EXCEPTION_WRITING_OBJECT",
                                                                           LOG), e);
                }
            }
        }
        message.getInterceptorChain().add(ending);
    }
    
    private class DispatchOutDatabindingEndingInterceptor extends AbstractOutDatabindingInterceptor {
        public DispatchOutDatabindingEndingInterceptor() {
            super(Phase.WRITE_ENDING);
        }
        
        public void handleMessage(Message message) throws Fault {
            OutputStream os = message.getContent(OutputStream.class);
            Object obj = message.getContent(Object.class);
            XMLStreamWriter xmlWriter = message.getContent(XMLStreamWriter.class);
            
            try {
                if (xmlWriter != null) {
                    xmlWriter.flush();
                } else if (obj instanceof SOAPMessage) {
                    ((SOAPMessage)obj).writeTo(os);
                } else if (obj instanceof Source || obj instanceof DataSource) {
                    doTransform(obj, os);
                }

                // Finish the message processing, do flush
                os.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new Fault(new org.apache.cxf.common.i18n.Message("EXCEPTION_WRITING_OBJECT", LOG));
            }
        }      
    }
        
    private SOAPMessage initSOAPMessage() throws SOAPException {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(WSDLConstants.NP_SCHEMA_XSD,
                                                                WSDLConstants.NU_SCHEMA_XSD);
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(WSDLConstants.NP_SCHEMA_XSI,
                                                                WSDLConstants.NU_SCHEMA_XSI);

        return msg;
    }

    private void doTransform(Object obj, OutputStream os) throws TransformerException, IOException {
        if (obj instanceof Source) {
            Transformer transformer = XMLUtils.newTransformer();
            transformer.transform((Source)obj, new StreamResult(os));
        }
        if (obj instanceof DataSource) {
            InputStream is = ((DataSource)obj).getInputStream();
            IOUtils.copy(((DataSource)obj).getInputStream(), os);
            is.close();
        }
    }

}
