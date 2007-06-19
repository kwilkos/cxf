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
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Service;

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
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.wsdl.WSDLConstants;

public class DispatchOutInterceptor extends AbstractOutDatabindingInterceptor {
    private static final Logger LOG = LogUtils.getL7dLogger(DispatchOutInterceptor.class);
    private DispatchOutEndingInterceptor ending;

    public DispatchOutInterceptor() {
        super(Phase.WRITE);
        ending = new DispatchOutEndingInterceptor();
    }

    public void handleMessage(Message message) throws Fault {
        Service.Mode m = message.getExchange().get(Service.Mode.class);
        OutputStream os = message.getContent(OutputStream.class);
        Object obj = message.getContent(Object.class);
        org.apache.cxf.service.Service service = 
            message.getExchange().get(org.apache.cxf.service.Service.class);
        
        if (obj == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("DISPATCH_OBJECT_CANNOT_BE_NULL", LOG));
        }

        try {
            if (message instanceof SoapMessage) {
                if (m == Service.Mode.MESSAGE) {
                    if (obj instanceof DataSource) {
                        throw new RuntimeException(obj.getClass()
                                                   + " is not valid in Message mode for SOAP/HTTP");
                    }
                } else if (m == Service.Mode.PAYLOAD) {
                    if (obj instanceof SOAPMessage || obj instanceof DataSource) {
                        throw new RuntimeException(obj.getClass()
                                                   + " is not valid in PAYLOAD mode with SOAP/HTTP");
                    }
                    
                    //Convert Source or JAXB element to SOAPMessage
                    SOAPMessage msg = initSOAPMessage();
                    DataWriter<Node> dataWriter = getDataWriter(message, service, Node.class);
                    dataWriter.write(obj, msg.getSOAPBody());
                    message.setContent(Object.class, msg);
                    message.setContent(SOAPMessage.class, msg);   
                    msg.writeTo(System.out);
                    //msg.writeTo(os);
                }
            } else if (message instanceof XMLMessage) {
                if (m == Service.Mode.MESSAGE
                    && obj instanceof SOAPMessage) {
                    throw new RuntimeException("SOAPMessage is not valid in MESSAGE mode with XML/HTTP");
                } else if (m == Service.Mode.PAYLOAD
                           && (obj instanceof SOAPMessage
                               || obj instanceof DataSource)) {
                    throw new RuntimeException(obj.getClass()
                                               + " is not valid in PAYLOAD mode with XML/HTTP");
                }                

                DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message, service,
                                                                       XMLStreamWriter.class);
                XMLStreamWriter xmlWriter = message.getContent(XMLStreamWriter.class);
                if (xmlWriter == null) {
                    xmlWriter = StaxUtils.createXMLStreamWriter(os, "UTF-8");
                }
                dataWriter.write(obj, xmlWriter);
                message.setContent(XMLStreamWriter.class, xmlWriter);
                // xmlWriter.flush();

            }
            // Finish the message processing, do flush
            // os.flush();
        } catch (Exception ex) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("EXCEPTION_WRITING_OBJECT", LOG));
        }
        
        message.getInterceptorChain().add(ending);
    }
    
    private class DispatchOutEndingInterceptor extends AbstractOutDatabindingInterceptor {
        public DispatchOutEndingInterceptor() {
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
