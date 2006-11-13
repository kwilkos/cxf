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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jbi.se.CXFServiceUnit;
import org.apache.cxf.jbi.se.CXFServiceUnitManager;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class JBIDestination implements Destination {

    private static final Logger LOG = LogUtils.getL7dLogger(JBIDestination.class);
    private final DeliveryChannel channel;
    private final CXFServiceUnitManager suManager; 
    private ConduitInitiator conduitInitiator;
    private EndpointInfo endpointInfo;
    private EndpointReferenceType reference;
    private MessageObserver incomingObserver;
    private JBIDispatcher dispatcher;
    private volatile boolean running; 
    
    public JBIDestination(ConduitInitiator ci,
                          EndpointInfo info,
                          DeliveryChannel dc,
                          CXFServiceUnitManager sum) {
        this.conduitInitiator = ci;
        this.endpointInfo = info;
        this.channel = dc;
        this.suManager = sum;
        reference = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(endpointInfo.getAddress());
        reference.setAddress(address);        
    }
    
    public EndpointReferenceType getAddress() {
        return reference;
    }

    public Conduit getBackChannel(Message inMessage, Message partialResponse, EndpointReferenceType address)
        throws IOException {
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

    public void shutdown() {
        running = false;
    }

    public void setMessageObserver(MessageObserver observer) {
        if (null != observer) {
            try {
                activate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.log(Level.FINE, "JBIDestination shutdown()");
            try {
                deactivate();
            } catch (IOException e) {
                //Ignore for now.
            }
        }
        incomingObserver = observer;

    }
    
    public MessageObserver getMessageObserver() {
        return incomingObserver;
    }

    private void deactivate() throws IOException {
        running = false;
    }

    private void activate() throws IOException {
        LOG.info(new org.apache.cxf.common.i18n.Message(
            "ACTIVE.JBI.SERVER.TRANSPORT", LOG).toString());
        dispatcher = new JBIDispatcher();
        new Thread(dispatcher).start();
    }
    
    // this should deal with the cxf message 
    protected class BackChannelConduit implements Conduit {
        
        protected Message inMessage;
        protected EndpointReferenceType target;
        protected JBIDestination jbiDestination;
                
        BackChannelConduit(EndpointReferenceType ref, Message message, JBIDestination dest) {
            inMessage = message;
            target = ref;
            jbiDestination = dest;
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
            message.put(JBIConstants.MESSAGE_EXCHANGE_PROPERTY, 
                inMessage.get(JBIConstants.MESSAGE_EXCHANGE_PROPERTY));
            message.setContent(OutputStream.class,
                               new JBIDestinationOutputStream(inMessage, channel));
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

    private class JBIDispatcher implements Runnable { 
        
        public final void run() {
            
            try { 
                running = true;
                LOG.info(new org.apache.cxf.common.i18n.Message(
                    "RECEIVE.THREAD.START", LOG).toString());
                do {
                    MessageExchange exchange = null;
                    synchronized (channel) {
                        exchange = channel.accept();
                    }
                                         
                    if (exchange != null) { 
                        ServiceEndpoint ep = exchange.getEndpoint();
                        
                        CXFServiceUnit csu = suManager.getServiceUnitForEndpoint(ep);
                        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                        
                        try { 
                            Thread.currentThread().setContextClassLoader(csu.getClassLoader());
                            if (csu != null) { 
                                LOG.info(new org.apache.cxf.common.i18n.Message(
                                    "DISPATCH.TO.SU", LOG).toString());
                                dispatch(exchange);
                            } else {
                                LOG.info(new org.apache.cxf.common.i18n.Message(
                                    "NO.SU.FOUND", LOG).toString());
                            }
                        } finally { 
                            Thread.currentThread().setContextClassLoader(oldLoader);
                        } 
                    } 
                } while(running);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, new org.apache.cxf.common.i18n.Message(
                    "ERROR.DISPATCH.THREAD", LOG).toString(), ex);
            } 
            LOG.fine(new org.apache.cxf.common.i18n.Message(
                "JBI.SERVER.TRANSPORT.MESSAGE.PROCESS.THREAD.EXIT", LOG).toString());
        }

    }
    
    private void dispatch(MessageExchange exchange) throws IOException {
        QName opName = exchange.getOperation(); 
        LOG.fine("dispatch method: " + opName);
        
        NormalizedMessage nm = exchange.getMessage("in");
        try {
            final InputStream in = JBIMessageHelper.convertMessageToInputStream(nm.getContent());
            //get the message to be interceptor
            MessageImpl inMessage = new MessageImpl();
            inMessage.put(JBIConstants.MESSAGE_EXCHANGE_PROPERTY, exchange);
            inMessage.setContent(InputStream.class, in);
                                           
            //dispatch to correct destination in case of multiple endpoint
            inMessage.setDestination(((JBITransportFactory)conduitInitiator).
                                     getDestination(exchange.getService().toString()
                                     + exchange.getInterfaceName().toString()));
            ((JBITransportFactory)conduitInitiator).
            getDestination(exchange.getService().toString()
                           + exchange.getInterfaceName().toString()).
                getMessageObserver().onMessage(inMessage);
            
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, new org.apache.cxf.common.i18n.Message(
                "ERROR.PREPARE.MESSAGE", LOG).toString(), ex);
            throw new IOException(ex.getMessage());
        }

    }
}
