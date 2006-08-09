package org.objectweb.celtix.endpoint;

import java.io.IOException;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.service.model.OperationInfo;

public class ClientImpl implements Client {

    Bus bus;
    Endpoint endpoint;
    
    public ClientImpl(Bus b, Endpoint e) {
        bus = b;
        endpoint = e;
    }
    

    public Object[] invoke(OperationInfo oi, Object[] params, Map<String, Object> ctx) {

        Message message = endpoint.getBinding().createMessage();
        message.setContent(Object[].class, params);
        setOutMessageProperties(message, oi);
   
        Exchange exchange = new ExchangeImpl();
        exchange.putAll(ctx);
        exchange.setOutMessage(message);
        setExchangeProperties(exchange, ctx);

        // setup chain
        
        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getOutPhases());
        chain.add(bus.getOutInterceptors());
        chain.add(endpoint.getService().getOutInterceptors());
        chain.add(endpoint.getOutInterceptors());
        chain.add(endpoint.getBinding().getOutInterceptors());
        
        modifyChain(chain, ctx);
        
        // setup conduit
        
        String transportID = endpoint.getEndpointInfo().getNamespaceURI();
        try {
            ConduitInitiator ci = bus.getConduitInitiatorManager().getConduitInitiator(transportID);
            ci.getConduit(null);
        } catch (BusException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        } catch (IOException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        } catch (WSDLException ex) {
            // TODO: can getConduitInitiator really throw a WSDLException?
            ex.printStackTrace();
        }

        // execute chain
        
        chain.doIntercept(message);
        
        // send message 
        
        Conduit conduit = exchange.getConduit();
        
        try {
            conduit.send(message);
        } catch (IOException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        }
        
        // correlate response
        
        if (conduit.getBackChannel() != null) {
            // process partial response and wait for decoupled response
        } else {
            // process response: send was synchronous so when we get here we can assume that the 
            // Exchange's inbound message is set and had been passed through the inbound interceptor chain.
        }
        
        
        return exchange.getInMessage().getContent(Object[].class);
    }
    
    
    protected void setOutMessageProperties(Message message, OperationInfo oi) {
        message.put(Message.OPERATION_INFO, oi);
        message.put(Message.BINDING, endpoint.getBinding());
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);
    }
    
    protected void setExchangeProperties(Exchange exchange, Map<String, Object> ctx) {
        // no-op
    }
    
    protected void modifyChain(InterceptorChain chain, Map<String, Object> ctx) {
        // no-op
    }

}
