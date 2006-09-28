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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.ConfigurationProvider;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.jms.destination.JMSDestinationConfigBean;
import org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType;
import org.apache.cxf.transports.jms.context.JMSMessageHeadersType;
import org.apache.cxf.transports.jms.jms_conf.JMSServerConfig;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;



public class JMSDestination extends JMSTransportBase implements Destination {
    static final Logger LOG = LogUtils.getL7dLogger(JMSDestination.class);
    final EndpointInfo endpointInfo;
    final EndpointReferenceType reference;
    final ConduitInitiator conduitInitiator;
    JMSDestinationConfigBean jmsDestinationConfigBean;
    PooledSession listenerSession;
    JMSListenerThread listenerThread;
    MessageObserver incomingObserver;
    
    public JMSDestination(Bus b,
                          ConduitInitiator ci,
                          EndpointInfo info) throws IOException {
        super(b, info, true);
        endpointInfo = info;
        initConfig();
        conduitInitiator = ci;
        reference = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(endpointInfo.getAddress());
        reference.setAddress(address);        
    }
    
    @Override
    public String getBeanName() {
        return endpointInfo.getName().toString() + ".jms-destination-base";
    }
    
    public EndpointReferenceType getAddress() {       
        return reference;
    }

    public Conduit getBackChannel(Message inMessage, 
                                  Message partialResponse, 
                                  EndpointReferenceType address) throws IOException {
                
        Conduit backChannel = null;
        if (address == null) {
            backChannel = new BackChannelConduit(address, inMessage, this);
        } else {
            if (partialResponse != null) {
                // just send back the partialResponse 
                backChannel = new BackChannelConduit(address, inMessage , this);
            } else {                
                backChannel = conduitInitiator.getConduit(endpointInfo, address);
                // ensure decoupled back channel input stream is closed
                backChannel.setMessageObserver(new MessageObserver() {
                    public void onMessage(Message m) {
                        //need to set up the headers 
                        if (m.getContentFormats().contains(InputStream.class)) {
                            InputStream is = m.getContent(InputStream.class);
                            try {
                                is.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                    }
                });
            }
        }
        return backChannel;
        
    }

  
    public void setMessageObserver(MessageObserver observer) {
        // to handle the incomming message        
        if (null != observer) {
            try {
                activate();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            LOG.log(Level.FINE, "JMSDestination shutdown()");
            try {
                deactivate();
            } catch (IOException e) {
                //Ignore for now.
            }
        }
        incomingObserver = observer;
    }
    
    private void activate() throws IOException {
        LOG.log(Level.INFO, "JMSServerTransport activate().... ");        

        try {
            LOG.log(Level.FINE, "establishing JMS connection");
            JMSProviderHub.connect(this, jmsDestinationConfigBean);
            //Get a non-pooled session. 
            listenerSession = sessionFactory.get(targetDestination);
            listenerThread = new JMSListenerThread(listenerSession, this);
            listenerThread.start();
        } catch (JMSException ex) {
            LOG.log(Level.FINE, "JMS connect failed with JMSException : ", ex);
            throw new IOException(ex.getMessage());
        } catch (NamingException nex) {
            LOG.log(Level.FINE, "JMS connect failed with NamingException : ", nex);
            throw new IOException(nex.getMessage());
        }
    }
    
    public void deactivate() throws IOException {
        try {
            listenerSession.consumer().close();
            if (listenerThread != null) {
                listenerThread.join();
            }
            sessionFactory.shutdown();
        } catch (InterruptedException e) {
            //Do nothing here
        } catch (JMSException ex) {
            //Do nothing here
        }
    }

    public void shutdown() {
        LOG.log(Level.FINE, "JMSDestination shutdown()");
        try {
            this.deactivate();
        } catch (IOException ex) {
            // Ignore for now.
        }         
    }

    public Queue getReplyToDestination(Message inMessage) 
        throws JMSException, NamingException {
        Queue replyTo;
        javax.jms.Message message = 
            (javax.jms.Message)inMessage.get(JMSConstants.JMS_REQUEST_MESSAGE);
        // If WS-Addressing had set the replyTo header.
        if  (inMessage.get(JMSConstants.JMS_REBASED_REPLY_TO) != null) {
            replyTo = sessionFactory.getQueueFromInitialContext(
                              (String)  inMessage.get(JMSConstants.JMS_REBASED_REPLY_TO));
        } else {
            replyTo = (null != message.getJMSReplyTo()) 
                ? (Queue)message.getJMSReplyTo() : (Queue)replyDestination;
        }    
        return replyTo;
    }
    
    public void setReplyCorrelationID(javax.jms.Message request, javax.jms.Message reply) 
        throws JMSException {
        
        String correlationID = request.getJMSCorrelationID();
        
        if (correlationID == null
            || "".equals(correlationID)
            && jmsDestinationConfigBean.getServer().isUseMessageIDAsCorrelationID()) {
            correlationID = request.getJMSMessageID();
        }
    
        if (correlationID != null && !"".equals(correlationID)) {
            reply.setJMSCorrelationID(correlationID);
        }
    }
    
    protected void incoming(javax.jms.Message message) throws IOException {
        try {
            LOG.log(Level.FINE, "server received request: ", message);           

            String msgType = message instanceof TextMessage 
                    ? JMSConstants.TEXT_MESSAGE_TYPE : JMSConstants.BINARY_MESSAGE_TYPE;
            Object request = unmarshal(message, msgType);
            LOG.log(Level.FINE, "The Request Message is [ " + request + "]");
            byte[] bytes = null;

            if (JMSConstants.TEXT_MESSAGE_TYPE.equals(msgType)) {
                String requestString = (String)request;
                LOG.log(Level.FINE, "server received request: ", requestString);
                bytes = requestString.getBytes();
            } else {
                bytes = (byte[])request;
            }

            // get the message to be interceptor
            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, new ByteArrayInputStream(bytes));
            JMSMessageHeadersType headers = 
                populateIncomingContext(message, inMessage, JMSConstants.JMS_SERVER_HEADERS);
            inMessage.put(JMSConstants.JMS_SERVER_HEADERS, headers);
            inMessage.put(JMSConstants.JMS_REQUEST_MESSAGE, message);
                        
            inMessage.setDestination(this);            
            
            //handle the incoming message
            incomingObserver.onMessage(inMessage);
           
        } catch (JMSException jmsex) {
            //TODO: need to revisit for which exception should we throw.
            throw new IOException(jmsex.getMessage());
        } 
    }
    
    private void initConfig() {
        
        final class JMSDestinationConfiguration extends JMSDestinationConfigBean {

            @Override
            public String getBeanName() {
                return endpointInfo.getName().toString() + ".jms-destination";
            }
        }
        JMSDestinationConfigBean bean = new JMSDestinationConfiguration();
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(bean);
        }
        
        if (!bean.isSetServer()) {
            bean.setServer(new JMSServerBehaviorPolicyType());
        }
        if (!bean.isSetServerConfig()) {
            bean.setServerConfig(new JMSServerConfig());
        }
        
        ConfigurationProvider p = new ServiceModelJMSConfigurationProvider(endpointInfo);
        List<ConfigurationProvider> providers = getOverwriteProviders();
        if (null == providers) {
            providers = new ArrayList<ConfigurationProvider>();
        }
        providers.add(p);
        setOverwriteProviders(providers);
        
        providers = bean.getOverwriteProviders();
        if (null == providers) {
            providers = new ArrayList<ConfigurationProvider>();
        }
        providers.add(p);
        bean.setOverwriteProviders(providers);

        jmsDestinationConfigBean = bean;
    }

    class JMSListenerThread extends Thread {
        final JMSDestination jmsDestination;
        private final PooledSession listenSession;

        public JMSListenerThread(PooledSession session,
                                 JMSDestination destination) {
            listenSession = session;
            jmsDestination = destination;
        }

        public void run() {
            try {
                while (true) {
                    javax.jms.Message message = listenSession.consumer().receive();                   
                    if (message == null) {
                        LOG.log(Level.WARNING,
                                "Null message received from message consumer.",
                                " Exiting ListenerThread::run().");
                        return;
                    }
                    while (message != null) {
                        //REVISIT  to get the thread pool                        
                        //Executor executor = jmsDestination.callback.getExecutor();
                        Executor executor = null;
                        if (executor == null) {
                            WorkQueueManager wqm = jmsDestination.bus.getExtension(WorkQueueManager.class);
                            if (null != wqm) {
                                executor = wqm.getAutomaticWorkQueue();
                            }    
                        }
                        if (executor != null) {
                            try {
                                executor.execute(new JMSExecutor(jmsDestination, message));
                                message = null;
                            } catch (RejectedExecutionException ree) {
                                //FIXME - no room left on workqueue, what to do
                                //for now, loop until it WILL fit on the queue, 
                                //although we could just dispatch on this thread.
                            }                            
                        } else {
                            LOG.log(Level.INFO, "handle the incoming message in listener thread");
                            try {
                                jmsDestination.incoming(message);
                            } catch (IOException ex) {
                                LOG.log(Level.WARNING, "Failed to process incoming message : ", ex);
                            }                            
                        }                        
                        message = null;
                    }
                }
            } catch (JMSException jmsex) {
                jmsex.printStackTrace();
                LOG.log(Level.SEVERE, "Exiting ListenerThread::run(): ", jmsex.getMessage());
            } catch (Throwable jmsex) {
                jmsex.printStackTrace();
                LOG.log(Level.SEVERE, "Exiting ListenerThread::run(): ", jmsex.getMessage());
            }
        }
    }
    
    static class JMSExecutor implements Runnable {
        javax.jms.Message message;
        JMSDestination jmsDestination;
        
        JMSExecutor(JMSDestination destionation, javax.jms.Message m) {
            message = m;
            jmsDestination = destionation;
        }

        public void run() {
            LOG.log(Level.INFO, "run the incoming message in the threadpool");
            try {
                jmsDestination.incoming(message);
            } catch (IOException ex) {
                //TODO: Decide what to do if we receive the exception.
                LOG.log(Level.WARNING,
                        "Failed to process incoming message : ", ex);
            }
        }
        
    }
    
    // this should deal with the cxf message 
    protected class BackChannelConduit implements Conduit {
        
        protected Message inMessage;
        protected EndpointReferenceType target;
        protected JMSDestination jmsDestination;
                
        BackChannelConduit(EndpointReferenceType ref, Message message, JMSDestination dest) {
            inMessage = message;
            target = ref;
            jmsDestination = dest;
        }
        
        public void close(Message msg) throws IOException {
            msg.getContent(OutputStream.class).close();        
        }

        /**
         * Register a message observer for incoming messages.
         * 
         * @param observer the observer to notify on receipt of incoming
         */
        public void setMessageObserver(MessageObserver observer) {
            // shouldn't be called for a back channel conduit
        }

        /**
         * Send an outbound message, assumed to contain all the name-value
         * mappings of the corresponding input message (if any). 
         * 
         * @param message the message to be sent.
         */
        public void send(Message message) throws IOException {
            // setup the message to be send back
            message.put(JMSConstants.JMS_REQUEST_MESSAGE, 
                        inMessage.get(JMSConstants.JMS_REQUEST_MESSAGE));
            message.setContent(OutputStream.class,
                               new JMSOutputStream(inMessage, jmsDestination));
        }
        
        /**
         * @return the reference associated with the target Destination
         */    
        public EndpointReferenceType getTarget() {
            return target;
        }
        
        /**
         * Retreive the back-channel Destination.
         * 
         * @return the backchannel Destination (or null if the backchannel is
         * built-in)
         */
        public Destination getBackChannel() {
            return null;
        }
        
        /**
         * Close the conduit
         */
        public void close() {
        }
    }
    
    private class JMSOutputStream extends AbstractCachedOutputStream {
                
        private Message inMessage;
        private JMSDestination jmsDestination;
        private javax.jms.Message reply;
        private Queue replyTo;
        private QueueSender sender;
        
        // setup the ByteArrayStream
        public JMSOutputStream(Message m, JMSDestination d) {
            super();
            inMessage = m;
            jmsDestination = d;
        }
        
        //to prepear the message and get the send out message
        private void commitOutputMessage() throws IOException {
            
            JMSMessageHeadersType headers =
                (JMSMessageHeadersType) inMessage.get(JMSConstants.JMS_SERVER_HEADERS);
            javax.jms.Message request = 
                (javax.jms.Message) inMessage.get(JMSConstants.JMS_REQUEST_MESSAGE);              
            
            PooledSession replySession = null;          
            
            if (jmsDestination.isDestinationStyleQueue()) {
                try {
                    //setup the reply message                
                    replyTo = getReplyToDestination(inMessage);
                    replySession = sessionFactory.get(false);
                    sender = (QueueSender)replySession.producer();
                    
                    boolean textPayload = request instanceof TextMessage 
                        ? true : false;
                    if (textPayload) {
                        
                        reply = marshal(currentStream.toString(), 
                                            replySession.session(), 
                                            null, 
                                            JMSConstants.TEXT_MESSAGE_TYPE);
                        LOG.log(Level.FINE, "The response message is [" + currentStream.toString() + "]");
                    } else {
                        reply = marshal(((ByteArrayOutputStream)currentStream).toByteArray(),
                                           replySession.session(),
                                           null, 
                                          JMSConstants.BINARY_MESSAGE_TYPE);
                        LOG.log(Level.FINE, "The response message is [" 
                                           + new String(((ByteArrayOutputStream)currentStream).toByteArray()) 
                                           + "]");
                    }     
                     
                    
                    setReplyCorrelationID(request, reply);
                    
                    setMessageProperties(headers, reply);

                    sendResponse();
                    
                } catch (JMSException ex) {
                    LOG.log(Level.WARNING, "Failed in post dispatch ...", ex);                
                    throw new IOException(ex.getMessage());                    
                } catch (NamingException nex) {
                    LOG.log(Level.WARNING, "Failed in post dispatch ...", nex);                
                    throw new IOException(nex.getMessage());                    
                } finally {
                    // house-keeping
                    if (replySession != null) {
                        sessionFactory.recycle(replySession);
                    }
                }
            } else {
                // we will never receive a non-oneway invocation in pub-sub
                // domain from Celtix client - however a mis-behaving pure JMS
                // client could conceivably make suce an invocation, in which
                // case we silently discard the reply
                LOG.log(Level.WARNING,
                        "discarding reply for non-oneway invocation ",
                        "with 'topic' destinationStyle");
                
            }        
            
            LOG.log(Level.FINE, "just server sending reply: ", reply);
            // Check the reply time limit Stream close will call for this
            
           
        }

        private void sendResponse() throws JMSException {
            JMSMessageHeadersType headers =
                (JMSMessageHeadersType) inMessage.get(JMSConstants.JMS_SERVER_HEADERS);
            javax.jms.Message request = 
                (javax.jms.Message) inMessage.get(JMSConstants.JMS_REQUEST_MESSAGE);   
            
            int deliveryMode = getJMSDeliveryMode(headers);
            int priority = getJMSPriority(headers);
            long ttl = getTimeToLive(headers);
            
            if (ttl <= 0) {
                ttl = jmsDestinationConfigBean.getServerConfig().getMessageTimeToLive();
            }
            
            long timeToLive = 0;
            if (request.getJMSExpiration() > 0) {
                TimeZone tz = new SimpleTimeZone(0, "GMT");
                Calendar cal = new GregorianCalendar(tz);
                timeToLive =  request.getJMSExpiration() - cal.getTimeInMillis();
            }
            
            if (timeToLive >= 0) {
                ttl = ttl > 0 ? ttl : timeToLive;
                LOG.log(Level.FINE, "send out the message!");
                sender.send(replyTo, reply, deliveryMode, priority, ttl);
            } else {
                // the request message had dead
                LOG.log(Level.INFO, "Message time to live is already expired skipping response.");
            }         
        }
           
        

        @Override
        protected void doFlush() throws IOException {
            // TODO Auto-generated method stub
            
        }

        
        @Override
        protected void doClose() throws IOException {
            
            commitOutputMessage();        
        }

        @Override
        protected void onWrite() throws IOException {
            // Do nothing here        
        }

    }

}
