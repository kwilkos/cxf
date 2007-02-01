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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.XMLMessage;
import org.apache.cxf.phase.Phase;

public class DispatchInInterceptor extends AbstractInDatabindingInterceptor {

    private static final Logger LOG = Logger.getLogger(DispatchInInterceptor.class.getName());
    
    public DispatchInInterceptor() {
        super();
        setPhase(Phase.READ);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) throws Fault {
        
        if (isGET(message)) {
            LOG.info("DispatchInInterceptor skipped in HTTP GET method");
            return;
        }
        
        Service.Mode m = message.getExchange().get(Service.Mode.class);
        Class type = message.getExchange().get(Class.class);

        try {
            resetContext(message);

            InputStream is = message.getContent(InputStream.class);

            if (message instanceof SoapMessage) {
                SOAPMessage soapMessage = newSOAPMessage(is, ((SoapMessage)message).getVersion());

                if (m == Service.Mode.MESSAGE) {
                    DataReader<SOAPMessage> dataReader = getDataReader(message, SOAPMessage.class);
                    message.setContent(Object.class, dataReader.read(null, soapMessage, type));
                } else if (m == Service.Mode.PAYLOAD) {
                    DataReader<SOAPBody> dataReader = getDataReader(message, SOAPBody.class);
                    message.setContent(Object.class, dataReader.read(null, soapMessage.getSOAPBody(), type));
                }
            } else if (message instanceof XMLMessage) {
                DataReader<XMLMessage> dataReader = getDataReader(message, XMLMessage.class);
                message.setContent(Object.class, 
                                   dataReader.read(null, (XMLMessage)message, type));                
            }
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    private void resetContext(Message message) throws JAXBException {
        JAXBContext context = message.getContent(JAXBContext.class);
        if (context != null) {
            org.apache.cxf.service.Service service = message.getExchange()
                .get(org.apache.cxf.service.Service.class);
            JAXBDataBinding dataBinding = new JAXBDataBinding();
            dataBinding.setContext(context);
            service.setDataBinding(dataBinding);
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
