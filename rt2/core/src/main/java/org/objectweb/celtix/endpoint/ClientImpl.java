package org.objectweb.celtix.endpoint;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.interceptors.AbstractBasicInterceptorProvider;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.interceptors.MessageSenderInterceptor;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.phase.PhaseManager;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public class ClientImpl extends AbstractBasicInterceptorProvider implements Client {
    
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

    public Object[] invoke(OperationInfo oi, Object[] params, Map<String, Object> ctx) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Invoke, operation info: " + oi + ", params: " + params);
        }
        Message message = endpoint.getBinding().createMessage();
        message.setContent(Object[].class, params);
        setOutMessageProperties(message, oi);
   
        Exchange exchange = new ExchangeImpl();
        if (null != ctx) {
            exchange.putAll(ctx);
        }
        exchange.setOutMessage(message);        
        // TODO: Set BindingOperationInfo here on exchange
        setExchangeProperties(exchange, ctx);
        message.setExchange(exchange);

        // setup chain
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
        
        List<Interceptor> il = bus.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);
        il = endpoint.getService().getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by service: " + il);
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
        
        /*
        chain.add(bus.getOutInterceptors());
        chain.add(endpoint.getService().getOutInterceptors());
        chain.add(endpoint.getOutInterceptors());
        chain.add(getOutInterceptors());
        chain.add(endpoint.getBinding().getOutInterceptors());
        */
        
        modifyChain(chain, ctx);
        
        // setup conduit
        Conduit conduit = getConduit();
        exchange.setConduit(conduit);

        // execute chain
        
        chain.doIntercept(message);

        // correlate response        
        if (conduit.getBackChannel() != null) {
            // process partial response and wait for decoupled response
        } else {
            // process response: send was synchronous so when we get here we can assume that the 
            // Exchange's inbound message is set and had been passed through the inbound interceptor chain.
        }

        // return exchange.getInMessage().getContent(Object[].class);
        return null;
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
    
    
    protected void setOutMessageProperties(Message message, OperationInfo oi) {
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);
    }
    
    protected void setExchangeProperties(Exchange exchange, Map<String, Object> ctx) {
        exchange.put(ExchangeConstants.ENDPOINT, endpoint);
        exchange.put(ExchangeConstants.BINDING, endpoint.getBinding());
    }
    
    protected void modifyChain(InterceptorChain chain, Map<String, Object> ctx) {
        // no-op
    }

}
