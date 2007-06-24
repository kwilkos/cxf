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

package org.apache.cxf.jaxws.handler.logical;


import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.wsdl.WSDLConstants;


public class LogicalMessageImpl implements LogicalMessage {

    private final LogicalMessageContextImpl msgContext;
    
    public LogicalMessageImpl(LogicalMessageContextImpl lmctx) {
        msgContext = lmctx;
    }

    public Source getPayload() {
        Source source = null;

        Service.Mode m = msgContext.getWrappedMessage().getExchange().get(Service.Mode.class);
        Message message = msgContext.getWrappedMessage();
        
        //Have to handle Dispatch differently
        if (m != null) {
            //Dispatch case
            Object obj = message.getContent(Object.class);
            if (message instanceof SoapMessage) {
                SOAPMessage soapMessage = (SOAPMessage)message.getContent(SOAPMessage.class);
               
                if (obj instanceof SOAPMessage || soapMessage != null) {
                    try {
                        if (!(obj instanceof SOAPMessage)) {
                            obj = soapMessage;
                        }
                        source = new DOMSource(((SOAPMessage)obj).getSOAPBody().getFirstChild());
                    } catch (SOAPException e) {
                        // ignore
                    }                  
                } else if (obj instanceof Source) {
                    try {
                        CachedOutputStream cos = new CachedOutputStream();

                        Transformer transformer = XMLUtils.newTransformer();
                        transformer.transform((Source)obj, new StreamResult(cos));
                        SOAPMessage msg = initSOAPMessage(cos.getInputStream());
                        source = new DOMSource(((SOAPMessage)msg).getSOAPBody().getFirstChild());
                    } catch (Exception e) {
                        // ignore
                    }                      
                }
            } else if (message instanceof XMLMessage) {
                if (obj instanceof Source) {
                    source = (Source)obj;
                } else if (obj instanceof DataSource) {
                    try {
                        source = new StreamSource(((DataSource)obj).getInputStream());
                    } catch (IOException e) {
                        // ignore
                    }
                } else {
                    //JAXBElement
                    W3CDOMStreamWriter xmlWriter = (W3CDOMStreamWriter)message
                        .getContent(XMLStreamWriter.class);
                    source = new DOMSource(xmlWriter.getDocument().getDocumentElement());               
                }              
            }
        } else {
            source = message.getContent(Source.class);
            if (source == null) {
                // need to convert
                SOAPMessage msg = message.getContent(SOAPMessage.class);
                XMLStreamReader reader = null;
                if (msg != null) {
                    try {
                        source = new DOMSource(msg.getSOAPBody().getFirstChild());
                        reader = StaxUtils.createXMLStreamReader(source);
                    } catch (SOAPException e) {
                        // ignore
                    }
                }

                if (source == null) {
                    try {
                        W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
                        reader = message.getContent(XMLStreamReader.class);
                        StaxUtils.copy(reader, writer);
                        source = new DOMSource(writer.getDocument().getDocumentElement());
                        reader = StaxUtils.createXMLStreamReader(writer.getDocument());
                    } catch (ParserConfigurationException e) {
                        throw new WebServiceException(e);
                    } catch (XMLStreamException e) {
                        throw new WebServiceException(e);
                    }
                }
                message.setContent(XMLStreamReader.class, reader);
                message.setContent(Source.class, source);
            } else if (!(source instanceof DOMSource)) {
                W3CDOMStreamWriter writer;
                try {
                    writer = new W3CDOMStreamWriter();
                } catch (ParserConfigurationException e) {
                    throw new WebServiceException(e);
                }
                XMLStreamReader reader = message.getContent(XMLStreamReader.class);
                if (reader == null) {
                    reader = StaxUtils.createXMLStreamReader(source);
                }
                try {
                    StaxUtils.copy(reader, writer);
                } catch (XMLStreamException e) {
                    throw new WebServiceException(e);
                }

                source = new DOMSource(writer.getDocument().getDocumentElement());

                reader = StaxUtils.createXMLStreamReader(writer.getDocument());
                message.setContent(XMLStreamReader.class, reader);
                message.setContent(Source.class, source);
            }
        }

        return source;
    }

    public void setPayload(Source s) {
        msgContext.getWrappedMessage().setContent(Source.class, s);
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(s);
        msgContext.getWrappedMessage().setContent(XMLStreamReader.class, reader);                
    }

    public Object getPayload(JAXBContext arg0) {
        try {
            return arg0.createUnmarshaller().unmarshal(getPayload());
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    public void setPayload(Object arg0, JAXBContext arg1) {
        try {
            W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
            arg1.createMarshaller().marshal(arg0, writer);
            Source source = new DOMSource(writer.getDocument().getDocumentElement());            
            
            setPayload(source);
        } catch (ParserConfigurationException e) {
            throw new WebServiceException(e);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

   
    public void write(Source source, Node n) {
        try {
            if (source instanceof DOMSource && ((DOMSource)source).getNode() == null) {
                return;
            }

            XMLStreamWriter writer = new W3CDOMStreamWriter((Element)n);
            XMLStreamReader reader = StaxUtils.createXMLStreamReader(source);
            StaxUtils.copy(reader, writer);
            reader.close();
        } catch (XMLStreamException e) {
            // throw new Fault(new Message("COULD_NOT_READ_XML_STREAM",
            // LOG), e);
        }
    }      
    
    private SOAPMessage initSOAPMessage(InputStream is) throws SOAPException, IOException {
        SOAPMessage msg = MessageFactory.newInstance().createMessage(null, is);
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(WSDLConstants.NP_SCHEMA_XSD,
                                                                WSDLConstants.NU_SCHEMA_XSD);
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(WSDLConstants.NP_SCHEMA_XSI,
                                                                WSDLConstants.NU_SCHEMA_XSI);

        return msg;
    }  
}
