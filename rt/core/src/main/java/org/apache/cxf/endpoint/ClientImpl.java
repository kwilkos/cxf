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

package org.apache.cxf.endpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.ClientOutFaultObserver;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.MessageObserver;

public class ClientImpl extends AbstractBasicInterceptorProvider implements Client, MessageObserver {
    public static final String FINISHED = "exchange.finished";
    
    private static final Logger LOG = LogUtils.getL7dLogger(ClientImpl.class);
    
    private Bus bus;
    private Endpoint endpoint;
    private Conduit initedConduit;
    private ClientOutFaultObserver outFaultObserver; 
    private int synchronousTimeout = 10000; // default 10 second timeout

    public ClientImpl(Bus b, Endpoint e) {
        this(b, e, null);
    }

    public ClientImpl(Bus b, Endpoint e, Conduit c) {
        bus = b;
        endpoint = e;
        outFaultObserver = new ClientOutFaultObserver(bus);
        if (null != c) {
            initedConduit = c;
        }
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    @SuppressWarnings("unchecked")
    public Object[] invoke(BindingOperationInfo oi, Object[] params, 
                           Map<String, Object> context) throws Exception {
        Map<String, Object> requestContext = null;
        Map<String, Object> responseContext = null;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Invoke, operation info: " + oi + ", params: " + params);
        }
        Message message = endpoint.getBinding().createMessage();
        if (null != context) {
            requestContext = (Map<String, Object>) context.get(REQUEST_CONTEXT);
            responseContext = (Map<String, Object>) context.get(RESPONSE_CONTEXT);
        }    
        //setup the message context
        setContext(requestContext, message);
        setParameters(params, message);
        Exchange exchange = new ExchangeImpl();

        if (null != requestContext) {
            exchange.putAll(requestContext);
        }
        exchange.setOneWay(oi.getOutput() == null);

        exchange.setOutMessage(message);
        
        setOutMessageProperties(message, oi);
        setExchangeProperties(exchange, requestContext, oi);
        
        // setup chain

        PhaseInterceptorChain chain = setupInterceptorChain();
        message.setInterceptorChain(chain);
        
        modifyChain(chain, requestContext);
        chain.setFaultObserver(outFaultObserver);
        // setup conduit
        Conduit conduit = getConduit();
        exchange.setConduit(conduit);
        conduit.setMessageObserver(this);
        
        //set clientImpl to exchange. used by jax-ws handlers
        exchange.put(Client.class, this);
        
        // execute chain
        chain.doIntercept(message);

        // Check to see if there is a Fault from the outgoing chain
        Exception ex = message.getContent(Exception.class);
        
        if (ex != null) {
            throw ex;
        }
        ex = message.getExchange().get(Exception.class);
        if (ex != null) {
            throw ex;
        }
        
        // Wait for a response if we need to
        if (!oi.getOperationInfo().isOneWay()) {
            synchronized (exchange) {
                waitResponse(exchange);
            }
        }

        // Grab the response objects if there are any
        List resList = null;
        Message inMsg = exchange.getInMessage();
        if (inMsg != null) {
            if (null != responseContext) {                   
                responseContext.putAll(inMsg);
                LOG.info("set responseContext to be" + responseContext);
            }
            resList = inMsg.getContent(List.class);
        }
        
        // check for an incoming fault
        ex = getException(exchange);
        
        if (ex != null) {
            throw ex;
        }
        
        if (resList != null) {
            return resList.toArray();
        }
        return null;
    }

    private Exception getException(Exchange exchange) {
        if (exchange.getInFaultMessage() != null) {
            return exchange.getInFaultMessage().getContent(Exception.class);
        } else if (exchange.getOutFaultMessage() != null) {
            return exchange.getOutFaultMessage().getContent(Exception.class);
        } 
        return null;
    }

    private void setContext(Map<String, Object> ctx, Message message) {
        if (ctx != null) {            
            message.putAll(ctx);
            LOG.info("set requestContext to message be" + ctx);
        }        
    }

    private void waitResponse(Exchange exchange) {
        int remaining = synchronousTimeout;
        while (!Boolean.TRUE.equals(exchange.get(FINISHED)) && remaining > 0) {
            long start = System.currentTimeMillis();
            try {
                exchange.wait(remaining);
            } catch (InterruptedException ex) {
                // ignore
            }
            long end = System.currentTimeMillis();
            remaining -= (int)(end - start);
        }
        if (!Boolean.TRUE.equals(exchange.get(FINISHED))) {
            LogUtils.log(LOG, Level.WARNING, "RESPONSE_TIMEOUT");
        }
    }

    private void setParameters(Object[] params, Message message) {
        if (params == null) {
            message.setContent(List.class, Collections.emptyList());
        } else {
            message.setContent(List.class, Arrays.asList(params));
        }
    }
    
    public void onMessage(Message message) {
        message = endpoint.getBinding().createMessage(message);
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);
        message.put(Message.INBOUND_MESSAGE, Boolean.TRUE);
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getInPhases());
        message.setInterceptorChain(chain);
        
        List<Interceptor> il = bus.getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);
        il = endpoint.getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by endpoint: " + il);
        }
        chain.add(il);
        il = getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by client: " + il);
        }
        chain.add(il);
        il = endpoint.getBinding().getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by binding: " + il);
        }
        chain.add(il);
        
        // execute chain
        try {
            String startingInterceptorID = (String) message.get(
                PhaseInterceptorChain.STARTING_AFTER_INTERCEPTOR_ID);
            if (startingInterceptorID != null) {
                chain.doIntercept(message, startingInterceptorID);
            } else {
                chain.doIntercept(message);
            }
        } finally {
            synchronized (message.getExchange()) {
                if (!isPartialResponse(message)) {
                    message.getExchange().put(FINISHED, Boolean.TRUE);
                    message.getExchange().setInMessage(message);
                    message.getExchange().notifyAll();
                }
            }
        }
    }

    private Conduit getConduit() {        
        if (null == initedConduit) {
            EndpointInfo ei = endpoint.getEndpointInfo();
            String transportID = ei.getTransportId();
            try {
                ConduitInitiator ci = bus.getExtension(ConduitInitiatorManager.class)
                    .getConduitInitiator(transportID);
                initedConduit = ci.getConduit(ei);
            } catch (BusException ex) {
                throw new Fault(ex);
            } catch (IOException ex) {
                throw new Fault(ex);
            }
        }
        return initedConduit;
    }

    protected void setOutMessageProperties(Message message, BindingOperationInfo boi) {
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);
        message.put(Message.INBOUND_MESSAGE, Boolean.FALSE);
        message.put(BindingMessageInfo.class, boi.getInput());
        message.put(MessageInfo.class, boi.getOperationInfo().getInput());
    }
    
    protected void setExchangeProperties(Exchange exchange,
                                         Map<String, Object> ctx,
                                         BindingOperationInfo boi) {
       
        exchange.put(Service.class, endpoint.getService());
        exchange.put(Endpoint.class, endpoint);
        exchange.put(ServiceInfo.class, endpoint.getService().getServiceInfo());
        exchange.put(InterfaceInfo.class, endpoint.getService().getServiceInfo().getInterface());
        exchange.put(Binding.class, endpoint.getBinding());
        exchange.put(BindingInfo.class, endpoint.getEndpointInfo().getBinding());
        exchange.put(BindingOperationInfo.class, boi);
        exchange.put(OperationInfo.class, boi.getOperationInfo());
    }

    protected PhaseInterceptorChain setupInterceptorChain() { 

        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
        
        List<Interceptor> il = bus.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);
        il = endpoint.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by endpoint: " + il);
        }
        chain.add(il);
        il = getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by client: " + il);
        }
        chain.add(il);
        il = endpoint.getBinding().getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by binding: " + il);
        }
        chain.add(il);        
        return chain;
    }

    protected void modifyChain(InterceptorChain chain, Map<String, Object> ctx) {
        // no-op
    }

    protected void setEndpoint(Endpoint e) {
        endpoint = e;
    }

    public int getSynchronousTimeout() {
        return synchronousTimeout;
    }

    public void setSynchronousTimeout(int synchronousTimeout) {
        this.synchronousTimeout = synchronousTimeout;
    }

    private boolean isPartialResponse(Message in) {
        return in.getContent(List.class) == null
               && getException(in.getExchange()) == null;
    }
}
