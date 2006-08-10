package org.objectweb.celtix.endpoint;

import java.io.IOException;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.interceptors.AbstractBasicInterceptorProvider;
import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.interceptors.MessageSenderInterceptor;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.service.model.OperationInfo;

public class ClientImpl extends AbstractBasicInterceptorProvider implements Client {

    Bus bus;
    Endpoint endpoint;
    
    public ClientImpl(Bus b, Endpoint e) {
        bus = b;
        endpoint = e;
        
        getOutInterceptors().add(new MessageSenderInterceptor());
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
        chain.add(getOutInterceptors());
        chain.add(endpoint.getBinding().getOutInterceptors());
        
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

        return exchange.getInMessage().getContent(Object[].class);
    }


    private Conduit getConduit() {
        String transportID = endpoint.getEndpointInfo().getTransportId();
        try {
            ConduitInitiator ci = bus.getConduitInitiatorManager().getConduitInitiator(transportID);
            return ci.getConduit(null);
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
        
        return null;
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
