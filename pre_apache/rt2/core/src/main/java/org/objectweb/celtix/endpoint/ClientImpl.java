package org.objectweb.celtix.endpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.interceptors.AbstractBasicInterceptorProvider;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.interceptors.MessageSenderInterceptor;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.phase.PhaseManager;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public class ClientImpl extends AbstractBasicInterceptorProvider implements Client, MessageObserver {
    
    private static final Logger LOG = Logger.getLogger(ClientImpl.class.getName());
    
    Bus bus;
    Endpoint endpoint;
    
    public ClientImpl(Bus b, Endpoint e) {
        bus = b;
        endpoint = e;
        
        getOutInterceptors().add(new MessageSenderInterceptor());
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public Object[] invoke(BindingOperationInfo oi, Object[] params, Map<String, Object> ctx) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Invoke, operation info: " + oi + ", params: " + params);
        }
        Message message = endpoint.getBinding().createMessage();
        if (params == null) {
            message.setContent(List.class, Collections.emptyList());
        } else {
            message.setContent(List.class, Arrays.asList(params));
        }
   
        Exchange exchange = new ExchangeImpl();
        if (null != ctx) {
            exchange.putAll(ctx);
        }

        exchange.setOutMessage(message);
        message.setExchange(exchange);
        
//        message.setContent(List.class, Arrays.asList(params));
        
        setOutMessageProperties(message, oi);
        setExchangeProperties(exchange, ctx, oi);
        

        // setup chain
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
        message.setInterceptorChain(chain);
        
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
        
        
        modifyChain(chain, ctx);
        
        // setup conduit
        Conduit conduit = getConduit();
        exchange.setConduit(conduit);
        conduit.setMessageObserver(this);
        
        // execute chain
        
        chain.doIntercept(message);

        if (message.getContent(Exception.class) != null) {
            //exception trying to send the message
            throw new RuntimeException(message.get(Exception.class));
        }
        
        // correlate response        
        if (conduit.getBackChannel() != null) {
            // process partial response and wait for decoupled response
        } else {
            // process response: send was synchronous so when we get here we can assume that the 
            // Exchange's inbound message is set and had been passed through the inbound interceptor chain.
        }

        if (oi.getOutput() != null) {
            synchronized (exchange) {
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
                    
                return inMsg.getContent(List.class).toArray();
            }
        } 
        return null;
    }


    public void onMessage(Message message) {
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
            chain.doIntercept(message);
        } finally {
            synchronized (message.getExchange()) {
                message.getExchange().setInMessage(message);
                message.getExchange().notifyAll();
            }
        }
    }

    private Conduit getConduit() {
        EndpointInfo ei = endpoint.getEndpointInfo();
        String transportID = ei.getTransportId();
        try {
            ConduitInitiator ci = bus.getExtension(ConduitInitiatorManager.class)
                .getConduitInitiator(transportID);
            return ci.getConduit(ei);
        } catch (BusException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        } catch (IOException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        }
        
        return null;
    }
    
    protected void setOutMessageProperties(Message message, BindingOperationInfo boi) {
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);
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
    
    protected void modifyChain(InterceptorChain chain, Map<String, Object> ctx) {
        // no-op
    }

}
