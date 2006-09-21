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

package org.apache.cxf.transport.jms;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.naming.NamingException;
import javax.wsdl.WSDLException;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.ConfigurationProvider;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;

import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.jms.conduit.JMSConduitConfigBean;
import org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType;
import org.apache.cxf.transports.jms.context.JMSMessageHeadersType;
import org.apache.cxf.transports.jms.jms_conf.JMSClientConfig;

import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class JMSConduit extends JMSTransportBase implements Conduit {
    private static final Logger LOG = LogUtils.getL7dLogger(JMSConduit.class);
    
      
    protected JMSConduitConfigBean jmsConduitConfigBean;   
    
    private MessageObserver incomingObserver;
    private EndpointReferenceType target;
   
    public JMSConduit(Bus b, EndpointInfo endpointInfo) {
        this(b, endpointInfo, null);
    }
    
    public JMSConduit(Bus b,
                      EndpointInfo endpointInfo,
                      EndpointReferenceType target) {           
        super(b, endpointInfo, false);        

        initConfig();
    } 

    // prepare the message for send out , not actually send out the message
    public void send(Message message) throws IOException {        
        LOG.log(Level.FINE, "JMSConduit send message");

        try {
            if (null == sessionFactory) {
                JMSProviderHub.connect(this, null);
            }
        } catch (JMSException jmsex) {
            LOG.log(Level.WARNING, "JMS connect failed with JMSException : ", jmsex);            
            throw new IOException(jmsex.toString());
        } catch (NamingException ne) {
            LOG.log(Level.WARNING, "JMS connect failed with NamingException : ", ne);
            throw new IOException(ne.toString());
        }

        if (sessionFactory == null) {
            throw new java.lang.IllegalStateException("JMSClientTransport not connected");
        }

        try {
            boolean isOneWay = false;        
            //test if the message is oneway message
            Exchange ex = message.getExchange();
            if (null != ex) {
                isOneWay = ex.isOneWay();
            }    
            //get the pooledSession with response expected 
            PooledSession pooledSession = sessionFactory.get(!isOneWay);            
            // put the PooledSession into the outMessage
            message.put(JMSConstants.JMS_POOLEDSESSION, pooledSession);
            
        } catch (JMSException jmsex) {
            throw new IOException(jmsex.getMessage());
        }
        
        message.setContent(OutputStream.class,
                           new JMSOutputStream(message));
      
    }

    public void close(Message message) throws IOException {
        // TODO Auto-generated method stub
        message.getContent(OutputStream.class).close();
        //using the outputStream to setup the corralated response
    }

    public EndpointReferenceType getTarget() {
        return target;
    }

    public Destination getBackChannel() {
        return null;
       //TODO now didn't support this asychronized request
    }

    public void close() {       
        LOG.log(Level.FINE, "JMSConduit closed ");

        // ensure resources held by session factory are released
        //
        if (sessionFactory != null) {
            sessionFactory.shutdown();
        }
    }

    public void setMessageObserver(MessageObserver observer) {
        incomingObserver = observer;        
        LOG.info("registering incoming observer: " + incomingObserver);        
    }
    
    
    /**
     * Receive mechanics.
     *
     * @param pooledSession the shared JMS resources
     * @retrun the response buffer
     */
    private Object receive(PooledSession pooledSession,
                           Message outMessage) throws JMSException {
        
        Object result = null;
        
        long timeout = jmsConduitConfigBean.getClientConfig().getClientReceiveTimeout();

        Long receiveTimeout = (Long)outMessage.get(JMSConstants.JMS_CLIENT_RECEIVE_TIMEOUT);

        if (receiveTimeout != null) {
            timeout = receiveTimeout.longValue();
        }
        
        javax.jms.Message jmsMessage = pooledSession.consumer().receive(timeout);
        LOG.log(Level.FINE, "client received reply: " , jmsMessage);

        if (jmsMessage != null) {
            
            populateIncomingContext(jmsMessage, outMessage, JMSConstants.JMS_CLIENT_RESPONSE_HEADERS);
            String messageType = jmsMessage instanceof TextMessage 
                        ? JMSConstants.TEXT_MESSAGE_TYPE : JMSConstants.BINARY_MESSAGE_TYPE;
            result = unmarshal(jmsMessage, messageType);
            return result;
        } else {
            String error = "JMSClientTransport.receive() timed out. No message available.";
            LOG.log(Level.SEVERE, error);
            //TODO: Review what exception should we throw.
            throw new JMSException(error);
            
        }
    }

    private void initConfig() {
        JMSConduitConfigBean bean = new JMSConduitConfigBean();
        if (!bean.isSetClient()) {
            bean.setClient(new JMSClientBehaviorPolicyType());
        }
        if (!bean.isSetClientConfig()) {
            bean.setClientConfig(new JMSClientConfig());
        }

        ConfigurationProvider p = new ServiceModelJMSConfigurationProvider(endpointInfo);
        List<ConfigurationProvider> providers = getOverwriteProviders();
        if (null == providers) {
            providers = new ArrayList<ConfigurationProvider>();
        }
        providers.add(p);
        setOverwriteProviders(providers);
        
        // providers = bean.getFallbackProviders();
        providers = bean.getOverwriteProviders();
        if (null == providers) {
            providers = new ArrayList<ConfigurationProvider>();
        }
        providers.add(p);
        // bean.setFallbackProviders(providers);
        bean.setOverwriteProviders(providers);

        jmsConduitConfigBean = bean;

    }

    private boolean isTextPayload() {
        return JMSConstants.TEXT_MESSAGE_TYPE.equals(
            jmsConduitConfigBean.getClient().getMessageType().value());
    }
    
    private class JMSOutputStream extends AbstractCachedOutputStream {
        private Message outMessage;
        private javax.jms.Message jmsMessage;
        private PooledSession pooledSession;
        private boolean isOneWay;
                
        public JMSOutputStream(Message m) {
            outMessage = m;
            pooledSession = (PooledSession)outMessage.get(JMSConstants.JMS_POOLEDSESSION);
        }     
        
        protected void doFlush() throws IOException {
          //do nothing here    
        }
        
        protected void doClose() throws IOException {            
            try {
                isOneWay = outMessage.getExchange().isOneWay();
                commitOutputMessage();
                if (!isOneWay) {
                    handleResponse();
                }
                sessionFactory.recycle(pooledSession);
            } catch (JMSException jmsex) {
                LOG.log(Level.WARNING, "JMS connect failed with JMSException : ", jmsex);            
                throw new IOException(jmsex.toString());
            }
        }
        
        protected void onWrite() throws IOException {
            
        }
        
        private void commitOutputMessage() throws JMSException {
            Object request = null;            
            
            if (isTextPayload()) {
                request = currentStream.toString();
            } else {
                request = ((ByteArrayOutputStream)currentStream).toByteArray();
            }
            
            LOG.log(Level.FINE, "Conduit Request is :[" + request + "]");
            javax.jms.Destination replyTo = pooledSession.destination();
            
            //TODO setting up the responseExpected
            
            
            //We don't want to send temp queue in
            //replyTo header for oneway calls
            if (isOneWay
                && (getAddressPolicy().getJndiReplyDestinationName() == null)) {
                replyTo = null;
            }

            jmsMessage = marshal(request, pooledSession.session(), replyTo,
                jmsConduitConfigBean.getClient().getMessageType().value());
            
            JMSMessageHeadersType headers =
                (JMSMessageHeadersType)outMessage.get(JMSConstants.JMS_CLIENT_REQUEST_HEADERS);

            int deliveryMode = getJMSDeliveryMode(headers);
            int priority = getJMSPriority(headers);
            String correlationID = getCorrelationId(headers);
            long ttl = getTimeToLive(headers);
            if (ttl <= 0) {
                ttl = jmsConduitConfigBean.getClientConfig().getMessageTimeToLive();
            }
            
            setMessageProperties(headers, jmsMessage);           
            
            if (!isOneWay) {
                String id = pooledSession.getCorrelationID();

                if (id != null) {
                    if (correlationID != null) {
                        String error = "User cannot set JMSCorrelationID when "
                            + "making a request/reply invocation using "
                            + "a static replyTo Queue.";
                        throw new JMSException(error);
                    }
                    correlationID = id;
                }
            }

            if (correlationID != null) {
                jmsMessage.setJMSCorrelationID(correlationID);
            } else {
                //No message correlation id is set. Whatever comeback will be accepted as responses.
                // We assume that it will only happen in case of the temp. reply queue.
            }

            LOG.log(Level.FINE, "client sending request: ",  jmsMessage);
            //getting  Destination Style
            if (isDestinationStyleQueue()) {
                QueueSender sender = (QueueSender)pooledSession.producer();
                sender.setTimeToLive(ttl);
                sender.send((Queue)targetDestination, jmsMessage, deliveryMode, priority, ttl);
            } else {
                TopicPublisher publisher = (TopicPublisher)pooledSession.producer();
                publisher.setTimeToLive(ttl);
                publisher.publish((Topic)targetDestination, jmsMessage, deliveryMode, priority, ttl);
            }
        }

        private void handleResponse() throws IOException {
            // REVISIT distinguish decoupled case or oneway call
            Object response = null;
            
            //TODO if outMessage need to get the response
            Message inMessage = new MessageImpl();
            inMessage.setExchange(outMessage.getExchange());
            //set the message header back to the incomeMessage
            inMessage.put(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS, 
                          outMessage.get(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS));
                        
            try {
                response = receive(pooledSession, outMessage);
            } catch (JMSException jmsex) {
                LOG.log(Level.FINE, "JMS connect failed with JMSException : ", jmsex);            
                throw new IOException(jmsex.toString());
            }  
            
            LOG.log(Level.FINE, "The Response Message is : [" + response + "]");
            
            // setup the inMessage response stream
            byte[] bytes = null;
            if (response instanceof String) {
                String requestString = (String)response;                
                bytes = requestString.getBytes();
            } else {
                bytes = (byte[])response;
            }
            inMessage.setContent(InputStream.class, new ByteArrayInputStream(bytes));
            LOG.log(Level.FINE, "incoming observer is " + incomingObserver);
            incomingObserver.onMessage(inMessage);
        }
    }

    
    /**
     * Represented decoupled response endpoint.
     */
    protected class DecoupledDestination implements Destination {
        protected MessageObserver decoupledMessageObserver;
        private EndpointReferenceType address;
        
        DecoupledDestination(EndpointReferenceType ref,
                             MessageObserver incomingObserver) {
            address = ref;
            decoupledMessageObserver = incomingObserver;
        }

        public EndpointReferenceType getAddress() {
            return address;
        }

        public Conduit getBackChannel(Message inMessage,
                                      Message partialResponse,
                                      EndpointReferenceType addr)
            throws WSDLException, IOException {
            // shouldn't be called on decoupled endpoint
            return null;
        }

        public void shutdown() {
            // TODO Auto-generated method stub            
        }

        public synchronized void setMessageObserver(MessageObserver observer) {
            decoupledMessageObserver = observer;
        }
        
        protected synchronized MessageObserver getMessageObserver() {
            return decoupledMessageObserver;
        }
    }     

}
