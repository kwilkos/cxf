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
import java.util.List;
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.w3c.dom.Node;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.source.NodeDataReader;
import org.apache.cxf.databinding.source.XMLStreamDataReader;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.handler.logical.DispatchLogicalHandlerInterceptor;
import org.apache.cxf.jaxws.handler.soap.DispatchSOAPHandlerInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.StaxUtils;

public class DispatchInDatabindingInterceptor extends AbstractInDatabindingInterceptor {

    private static final Logger LOG = Logger.getLogger(DispatchInDatabindingInterceptor.class.getName());
    private Class type;
    private Service.Mode mode;
    
    public DispatchInDatabindingInterceptor(Class type, Mode mode) {
        super(Phase.READ);
        
        this.type = type;
        this.mode = mode;
    }

    public void handleMessage(Message message) throws Fault {
        Exchange ex = message.getExchange();     
        Endpoint ep = ex.get(Endpoint.class);
        MessageInfo info = message.get(MessageInfo.class);
        if (ep.getEndpointInfo().getBinding().getOperations().iterator().hasNext()) {
            BindingOperationInfo bop = ep.getEndpointInfo().getBinding().getOperations().iterator().next();
            ex.put(BindingOperationInfo.class, bop);
            info = getMessageInfo(message, bop);
        }      
        
        if (isGET(message)) {
            MessageContentsList params = new MessageContentsList();          
            if (info != null) {
                for (MessagePartInfo i : info.getMessageParts()) {
                    params.put(i, null);
                }
            } else {
                params.add(null);
            }
            message.setContent(List.class, params);
            LOG.info("DispatchInInterceptor skipped in HTTP GET method");
            return;
        }       
     
        try {
            InputStream is = message.getContent(InputStream.class);
            Object obj = null;
            ex.put(Service.Mode.class, mode);            
            
            if (message instanceof SoapMessage) {
                SOAPMessage soapMessage = newSOAPMessage(is, ((SoapMessage)message).getVersion());
                PostDispatchSOAPHandlerInterceptor postSoap = new PostDispatchSOAPHandlerInterceptor();
                message.getInterceptorChain().add(postSoap);
                
                //soapMessage.writeTo(System.out);
                message.setContent(SOAPMessage.class, soapMessage);               
            } else if (message instanceof XMLMessage) {
                if (type.equals(DataSource.class)) {
                    try {
                        obj = new ByteArrayDataSource(is, (String) message.get(Message.CONTENT_TYPE));
                    } catch (IOException e) {
                        throw new Fault(e);
                    }
                    //Treat DataSource specially here as it is not valid to call getPayload from 
                    //LogicalHandler for DataSource payload
                    message.setContent(DataSource.class, obj);  
                } else {                 
                    new StaxInInterceptor().handleMessage(message);
                    
                    DataReader<XMLStreamReader> dataReader = new XMLStreamDataReader();
                    Class readType = type;
                    if (!Source.class.isAssignableFrom(type)) {
                        readType = Source.class;
                    }
                    obj = dataReader.read(null, message.getContent(XMLStreamReader.class), readType);
                    message.setContent(Source.class, obj); 
                }
            }
    
            PostDispatchLogicalHandlerInterceptor postLogical = new PostDispatchLogicalHandlerInterceptor();
            message.getInterceptorChain().add(postLogical);      
            
            is.close();
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    private SOAPMessage newSOAPMessage(InputStream is, SoapVersion version) throws Exception {
        // TODO: Get header from message, this interceptor should after
        // readHeadersInterceptor

        MimeHeaders headers = new MimeHeaders();
        MessageFactory msgFactory = null;
        if (version == null || version instanceof Soap11) {
            msgFactory = MessageFactory.newInstance();
        } else if (version instanceof Soap12) {
            msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        }
        return msgFactory.createMessage(headers, is);
    }
    
    //This interceptor is invoked after DispatchSOAPHandlerInterceptor, converts SOAPMessage to Source
    private class PostDispatchSOAPHandlerInterceptor extends AbstractInDatabindingInterceptor {

        public PostDispatchSOAPHandlerInterceptor() {
            super(Phase.USER_PROTOCOL);
            addAfter(DispatchSOAPHandlerInterceptor.class.getName());
        }

        public void handleMessage(Message message) throws Fault {
            Object obj = null;
            
            //Convert SOAPMessage to Source
            if (message instanceof SoapMessage) {
                SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
                message.removeContent(SOAPMessage.class);

                DataReader<Node> dataReader = new NodeDataReader();
                Node n = null;
                if (mode == Service.Mode.MESSAGE) {
                    try {
                        n = soapMessage.getSOAPPart();
                        //This seems to be a problem in SAAJ. Envelope might not be initialized properly 
                        //without calling getEnvelope()
                        soapMessage.getSOAPPart().getEnvelope();
                    } catch (SOAPException e) {
                        throw new Fault(e);
                    } 
                } else if (mode == Service.Mode.PAYLOAD) {
                    try {
                        n = DOMUtils.getChild(soapMessage.getSOAPBody(), Node.ELEMENT_NODE);
                    } catch (SOAPException e) {
                        throw new Fault(e);
                    }
                }
                
                Class tempType = type;
                if (!Source.class.isAssignableFrom(type)) {
                    tempType = Source.class;
                }
                obj = dataReader.read(null, n, tempType);

                message.setContent(Source.class, obj);
            }
        }
    }
    
    //This interceptor is invoked after DispatchLogicalHandlerInterceptor, converts Source to object
    private class PostDispatchLogicalHandlerInterceptor extends AbstractInDatabindingInterceptor {

        public PostDispatchLogicalHandlerInterceptor() {
            super(Phase.USER_LOGICAL);
            addAfter(DispatchLogicalHandlerInterceptor.class.getName());            
        }

        public void handleMessage(Message message) throws Fault {
            Object obj = null;

            //Convert Source to object
            if (message instanceof SoapMessage) {
                Source source = message.getContent(Source.class);
                message.removeContent(Source.class);

                if (SOAPMessage.class.isAssignableFrom(type)) {
                    try {
                        CachedOutputStream cos = new CachedOutputStream();
                        Transformer transformer = XMLUtils.newTransformer();
                        transformer.transform(source, new StreamResult(cos));
                        obj = newSOAPMessage(cos.getInputStream(), ((SoapMessage)message).getVersion());
                    } catch (Exception e) {
                        throw new Fault(e);
                    } 
                } else if (Source.class.isAssignableFrom(type)) {
                    obj = source;                
                } else {
                    //JAXB  
                    try {                        
                        obj = convertSourceToJaxb(source, message);                        
                    } catch (Exception e) {
                        throw new Fault(e);
                    }
                }             
            } else if (message instanceof XMLMessage) {
                Source source = message.getContent(Source.class);
                message.removeContent(Source.class);
                DataSource dataSource = message.getContent(DataSource.class);
                message.removeContent(DataSource.class);
                
                if (source != null) {
                    if (Source.class.isAssignableFrom(type)) {
                        obj = (Source)source;
                    } else {
                        //jaxb
                        try {                        
                            obj = convertSourceToJaxb(source, message);                        
                        } catch (Exception e) {
                            throw new Fault(e);
                        }
                    }
                } else if (dataSource != null && DataSource.class.isAssignableFrom(type)) {
                    obj = (DataSource)dataSource;
                }
                    
            }
            message.setContent(Object.class, obj);
        }
        
    }
    
    private Object convertSourceToJaxb(Source source, Message message) throws Exception {
        CachedOutputStream cos = new CachedOutputStream();
        Transformer transformer = XMLUtils.newTransformer();
        transformer.transform(source, new StreamResult(cos));
        String encoding = (String)message.get(Message.ENCODING);

        XMLStreamReader reader = null;

        reader = StaxUtils.getXMLInputFactory().createXMLStreamReader(cos.getInputStream(),
                                                                      encoding);

        DataReader<XMLStreamReader> dataReader = getDataReader(message);
        dataReader.setProperty(JAXBDataBinding.UNWRAP_JAXB_ELEMENT, Boolean.FALSE);

        Object obj = dataReader.read(null, reader, null);
        
        return obj;
        
        //not sure why code below does not work                        
/*                        
        DataReader<XMLStreamReader> dataReader1 = 
            getDataReader(message, XMLStreamReader.class);
        XMLStreamReader reader1 = 
            StaxUtils.getXMLInputFactory().createXMLStreamReader(source);
        dataReader.setProperty(JAXBDataBinding.UNWRAP_JAXB_ELEMENT, Boolean.FALSE);
        obj = dataReader1.read(null, reader1, null);*/       
    }

}
