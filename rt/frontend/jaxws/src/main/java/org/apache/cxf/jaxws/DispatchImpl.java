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

package org.apache.cxf.jaxws;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.ConduitSelector;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.UpfrontConduitSelector;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.jaxws.handler.logical.DispatchLogicalHandlerOutInterceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerInInterceptor;
import org.apache.cxf.jaxws.handler.soap.DispatchSOAPHandlerInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.interceptors.DispatchInInterceptor;
import org.apache.cxf.jaxws.interceptors.DispatchOutInterceptor;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.transport.MessageObserver;

public class DispatchImpl<T> extends BindingProviderImpl implements Dispatch<T>, MessageObserver {
    private static final Logger LOG = LogUtils.getL7dLogger(DispatchImpl.class);

    private Bus bus;

    private Class<T> cl;
    private Executor executor;
    private JAXBContext context;
    private Service.Mode mode;

    private ConduitSelector conduitSelector;
    
    DispatchImpl(Bus b, Service.Mode m, Class<T> clazz, Executor e, Endpoint ep) {
        this(b, m, null, clazz, e, ep);
    }

    DispatchImpl(Bus b, Service.Mode m, JAXBContext ctx, Class<T> clazz, Executor e, Endpoint ep) {
        super(((JaxWsEndpointImpl)ep).getJaxwsBinding());
        bus = b;
        executor = e;
        context = ctx;
        cl = clazz;
        mode = m;
        getConduitSelector().setEndpoint(ep);
        setupEndpointAddressContext(ep);
    }

    private void setupEndpointAddressContext(Endpoint endpoint) {
        //NOTE for jms transport the address would be null
        if (null != endpoint
            && null != endpoint.getEndpointInfo().getAddress()) {
            Map<String, Object> requestContext = this.getRequestContext();
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                           endpoint.getEndpointInfo().getAddress());
        }    
    }
    public T invoke(T obj) {
        return invoke(obj, false);
    }

    public T invoke(T obj, boolean isOneWay) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Dispatch: invoke called");
        }

        Endpoint endpoint = getEndpoint();
        Message message = endpoint.getBinding().createMessage();

        if (context != null) {
            message.setContent(JAXBContext.class, context);
        }
        
        
        Map<String, Object> reqContext = new HashMap<String, Object>(this.getRequestContext());
        Map<String, Object> respContext = this.getResponseContext();
        // clear the response context's hold information
        // Not call the clear Context is to avoid the error 
        // that getResponseContext() would be called by Client code first
        respContext.clear();
        
        ContextPropertiesMapping.mapRequestfromJaxws2Cxf(reqContext);
        message.putAll(reqContext);
        //need to do context mapping from jax-ws to cxf message
        
        Exchange exchange = new ExchangeImpl();

        exchange.setOutMessage(message);
        setExchangeProperties(exchange, endpoint);

        message.setContent(Object.class, obj);
        
        if (obj instanceof SOAPMessage) {
            message.setContent(SOAPMessage.class, obj);
        } else if (obj instanceof Source) {
            message.setContent(Source.class, obj);
        } else if (obj instanceof DataSource) {
            message.setContent(DataSource.class, obj);
        }
  
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);

        PhaseInterceptorChain chain = getDispatchOutChain(endpoint);
        message.setInterceptorChain(chain);

        // setup conduit selector
        prepareConduitSelector(message);
        
        // execute chain
        chain.doIntercept(message);
        
        getConduitSelector().complete(exchange);
                
        if (message.getContent(Exception.class) != null) {
            if (getBinding() instanceof SOAPBinding) {
                try {
                    SOAPFault soapFault = SOAPFactory.newInstance().createFault();
                    Fault fault = (Fault)message.getContent(Exception.class);
                    soapFault.setFaultCode(fault.getFaultCode());
                    soapFault.setFaultString(fault.getMessage());
                    SOAPFaultException exception = new SOAPFaultException(soapFault);
                    throw exception;
                } catch (SOAPException e) {
                    throw new WebServiceException(e);
                }
            } else if (getBinding() instanceof HTTPBinding) {
                HTTPException exception = new HTTPException(HttpURLConnection.HTTP_INTERNAL_ERROR);
                exception.initCause(message.getContent(Exception.class));
                throw exception;
            } else {
                throw new WebServiceException(message.getContent(Exception.class));
            }
        }

        // correlate response        
        if (getConduitSelector().selectConduit(message).getBackChannel() != null) {
            // process partial response and wait for decoupled response
        } else {
            // process response: send was synchronous so when we get here we can assume that the 
            // Exchange's inbound message is set and had been passed through the inbound interceptor chain.
        }

        if (!isOneWay) {
            synchronized (exchange) {
                Message inMsg = waitResponse(exchange);
                respContext.putAll(inMsg);
                //need to do context mapping from cxf message to jax-ws 
                ContextPropertiesMapping.mapResponsefromCxf2Jaxws(respContext);
                return cl.cast(inMsg.getContent(Object.class));
            }
        }
        return null;
        
    }

    private Message waitResponse(Exchange exchange) {
        Message inMsg = exchange.getInMessage();
        if (inMsg == null) {
            try {
                exchange.wait();
            } catch (InterruptedException e) {
                //TODO - timeout
            }
            inMsg = exchange.getInMessage();
        }
        if (inMsg.getContent(Exception.class) != null) {
            //TODO - exceptions 
            throw new RuntimeException(inMsg.getContent(Exception.class));
        }
        return inMsg;
    }

    private PhaseInterceptorChain getDispatchOutChain(Endpoint endpoint) {
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());

        List<Interceptor> il = bus.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);
        
        if (endpoint instanceof JaxWsEndpointImpl) {
            Binding jaxwsBinding = ((JaxWsEndpointImpl)endpoint).getJaxwsBinding();
            if (endpoint.getBinding() instanceof SoapBinding) {
                chain.add(new DispatchSOAPHandlerInterceptor(jaxwsBinding));
            } else {
                // TODO: what for non soap bindings?
            }       
            chain.add(new DispatchLogicalHandlerOutInterceptor(jaxwsBinding));
        }   
        
        chain.add(new MessageSenderInterceptor());

        chain.add(new DispatchOutInterceptor());
        return chain;
    }

    public void onMessage(Message message) {
        Endpoint endpoint = getEndpoint();
        message = endpoint.getBinding().createMessage(message);

        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);

        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getInPhases());
        message.setInterceptorChain(chain);

        List<Interceptor> il = bus.getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);

        if (endpoint instanceof JaxWsEndpointImpl) {
            Binding jaxwsBinding = ((JaxWsEndpointImpl)endpoint).getJaxwsBinding();
            if (endpoint.getBinding() instanceof SoapBinding) {
                chain.add(new SOAPHandlerInterceptor(jaxwsBinding));
            }      
            chain.add(new LogicalHandlerInInterceptor(jaxwsBinding));
        }           

        List<Interceptor> inInterceptors = new ArrayList<Interceptor>();
        inInterceptors.add(new DispatchInInterceptor(cl, mode));
        chain.add(inInterceptors);

        // execute chain
        try {
            chain.doIntercept(message);
        } finally {
            synchronized (message.getExchange()) {
                message.getExchange().setInMessage(message);
                message.getExchange().notifyAll();
            }
        }
    }

    private Executor getExecutor() {
        if (executor == null) {
            executor = getEndpoint().getService().getExecutor();
        }
        if (executor == null) {
            executor = Executors.newFixedThreadPool(5);
        }
        if (executor == null) {
            System.err.println("Can't not get executor");
        }
        return executor;
    }
    
    private Endpoint getEndpoint() {
        return getConduitSelector().getEndpoint();
    }

    public Future<?> invokeAsync(T obj, AsyncHandler<T> asyncHandler) {
        Response<?> r = invokeAsync(obj);
        AsyncCallbackFuture callback = new AsyncCallbackFuture(r, asyncHandler);

        getExecutor().execute(callback);
        return callback;
    }

    public Response<T> invokeAsync(T obj) {
        FutureTask<T> f = new FutureTask<T>(new DispatchAsyncCallable<T>(this, obj));

        getExecutor().execute(f);
        return new AsyncResponse<T>(f, cl);
    }

    public void invokeOneWay(T obj) {
        invoke(obj, true);
    }
        
    public synchronized ConduitSelector getConduitSelector() {
        if (null == conduitSelector) {
            conduitSelector = new UpfrontConduitSelector();
        }
        return conduitSelector;
    }

    public void setConduitSelector(ConduitSelector selector) {
        conduitSelector = selector;
    }
    
    protected void prepareConduitSelector(Message message) {
        message.getExchange().put(ConduitSelector.class, getConduitSelector());
    }
    
    protected void setExchangeProperties(Exchange exchange, Endpoint endpoint) {
        exchange.put(Service.Mode.class, mode);
        exchange.put(Class.class, cl);
        exchange.put(org.apache.cxf.service.Service.class, endpoint.getService());
        exchange.put(Endpoint.class, endpoint);
        
        exchange.put(MessageObserver.class, this);
        exchange.put(Bus.class, bus);
    }

}
