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

package org.apache.cxf.jbi.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.jws.WebService;
import javax.xml.namespace.QName;
//import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
//import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;

public class JBIConduitOutputStream extends AbstractCachedOutputStream {

    private static final Logger LOG = LogUtils.getL7dLogger(JBIConduitOutputStream.class);

    private Message message;
    private boolean isOneWay;
    private DeliveryChannel channel;
    private JBIConduit conduit;
    private EndpointReferenceType target;

    public JBIConduitOutputStream(Message m, DeliveryChannel channel, EndpointReferenceType target,
                                  JBIConduit conduit) {
        message = m;
        this.channel = channel;
        this.conduit = conduit;
        this.target = target;
        
    }

    @Override
    protected void doFlush() throws IOException {

    }

    @Override
    protected void doClose() throws IOException {
        isOneWay = message.getExchange().isOneWay();
        commitOutputMessage();
        if (target != null) {
            target.getClass();
        }
    }

    private void commitOutputMessage() throws IOException {
        try {
            Method targetMethod = (Method)message.get(Method.class.getName());
            Class<?> clz = targetMethod.getDeclaringClass();

            LOG.info(new org.apache.cxf.common.i18n.Message("INVOKE.SERVICE", LOG).toString() + clz);

            WebService ws = clz.getAnnotation(WebService.class);
            assert ws != null;
            QName interfaceName = new QName(ws.targetNamespace(), ws.name());
            QName serviceName = null;
            if (target != null) {
                serviceName = EndpointReferenceUtils.getServiceName(target);
            } else {
                serviceName = message.getExchange().get(org.apache.cxf.service.Service.class).
                getServiceInfo().getName();
            }

            
            
          
          
            MessageExchangeFactory factory = channel.createExchangeFactoryForService(serviceName);
            LOG.info(new org.apache.cxf.common.i18n.Message("CREATE.MESSAGE.EXCHANGE", LOG).toString()
                     + serviceName);
            MessageExchange xchng = null;
            if (isOneWay) {
                xchng = factory.createInOnlyExchange();
            } else {
                xchng = factory.createInOutExchange();
            }

            NormalizedMessage inMsg = xchng.createMessage();
            LOG.info(new org.apache.cxf.common.i18n.Message("EXCHANGE.ENDPOINT", LOG).toString()
                     + xchng.getEndpoint());

            InputStream ins = null;

            if (inMsg != null) {
                LOG.info("setup message contents on " + inMsg);
                inMsg.setContent(getMessageContent(message));
                xchng.setService(serviceName);
                LOG.info("service for exchange " + serviceName);

                xchng.setInterfaceName(interfaceName);

                xchng.setOperation(new QName(targetMethod.getName()));
                if (isOneWay) {
                    ((InOnly)xchng).setInMessage(inMsg);
                } else {
                    ((InOut)xchng).setInMessage(inMsg);
                }
                LOG.info("sending message");
                if (!isOneWay) {

                    channel.sendSync(xchng);
                    NormalizedMessage outMsg = ((InOut)xchng).getOutMessage();
                    
                    //revisit later
                    /*Source content = outMsg.getContent();
                    XMLStreamReader reader = StaxUtils.createXMLStreamReader(content);
                    message.setContent(XMLStreamReader.class, reader);*/
                    
                    ins = JBIMessageHelper.convertMessageToInputStream(outMsg.getContent());
                    if (ins == null) {
                        throw new IOException(new org.apache.cxf.common.i18n.Message(
                            "UNABLE.RETRIEVE.MESSAGE", LOG).toString());
                    }
                    Message inMessage = new MessageImpl();
                    message.getExchange().setInMessage(inMessage);
                    inMessage.setContent(InputStream.class, ins);
                    conduit.getMessageObserver().onMessage(inMessage);
                    
                } else {
                    channel.send(xchng);
                }

            } else {
                LOG.info(new org.apache.cxf.common.i18n.Message("NO.MESSAGE", LOG).toString());
            }

            

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.toString());
        }
    }

    private Source getMessageContent(Message message2) {
        ByteArrayOutputStream bos = (ByteArrayOutputStream)getOut();
        return new StreamSource(new ByteArrayInputStream(bos.toByteArray()));
        
    }

    @Override
    protected void onWrite() throws IOException {

    }

}
