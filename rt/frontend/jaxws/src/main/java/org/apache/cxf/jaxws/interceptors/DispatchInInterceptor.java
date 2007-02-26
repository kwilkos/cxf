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

import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.w3c.dom.Node;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.Phase;

public class DispatchInInterceptor extends AbstractInDatabindingInterceptor {

    private static final Logger LOG = Logger.getLogger(DispatchInInterceptor.class.getName());
    private Class type;
    private Service.Mode mode;
    
    public DispatchInInterceptor(Class type, Mode mode) {
        super();
        setPhase(Phase.READ);
        
        this.type = type;
        this.mode = mode;
    }

    public void handleMessage(Message message) throws Fault {
        
        if (isGET(message)) {
            LOG.info("DispatchInInterceptor skipped in HTTP GET method");
            return;
        }

        try {
            InputStream is = message.getContent(InputStream.class);

            if (message instanceof SoapMessage) {
                SOAPMessage soapMessage = newSOAPMessage(is, ((SoapMessage)message).getVersion());

                Object obj;
                if (type.equals(SOAPMessage.class)) {
                    obj = soapMessage;
                } else if (type.equals(SOAPBody.class)) {
                    obj = soapMessage.getSOAPBody();
                } else {
                    DataReader<Node> dataReader = getDataReader(message, Node.class);
                    Node n = null;
                    if (mode == Service.Mode.MESSAGE) {
                        n = soapMessage.getSOAPPart();
                    } else if (mode == Service.Mode.PAYLOAD) {
                        n = DOMUtils.getChild(soapMessage.getSOAPBody(), Node.ELEMENT_NODE);
                    }
                    if (Source.class.isAssignableFrom(type)) {
                        obj = dataReader.read(null, n, type);
                    } else {
                        dataReader.setProperty(JAXBDataBinding.UNWRAP_JAXB_ELEMENT, Boolean.FALSE);
                        obj = dataReader.read(n);
                    }
                }
                message.setContent(Object.class, obj);
            } else if (message instanceof XMLMessage) {
                new StaxInInterceptor().handleMessage(message);
                
                DataReader<XMLStreamReader> dataReader = getDataReader(message);
                message.setContent(Object.class, dataReader.read(null, message
                    .getContent(XMLStreamReader.class), type));
            }
            
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
}
