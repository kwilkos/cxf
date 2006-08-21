package org.objectweb.celtix.messaging;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.phase.PhaseManager;
import org.objectweb.celtix.service.Service;

public class ChainInitiationObserver implements MessageObserver {
    Endpoint endpoint;
    Bus bus;

    public ChainInitiationObserver(Endpoint endpoint, Bus bus) {
        super();
        this.endpoint = endpoint;
        this.bus = bus;
    }

    public void onMessage(Message m) {
        Message message = endpoint.getBinding().createMessage(m);
        Exchange exchange = new ExchangeImpl();
        exchange.setInMessage(message);
        message.setExchange(exchange);
        
        exchange.put(Endpoint.class, endpoint);
        exchange.put(Service.class, endpoint.getService());
        exchange.put(Binding.class, endpoint.getBinding());
        exchange.put(Bus.class, bus);
        exchange.setDestination(m.getDestination());
        
        // setup chain
        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getExtension(PhaseManager.class)
            .getInPhases());
        chain.add(bus.getInInterceptors());
        chain.add(endpoint.getInInterceptors());
        chain.add(endpoint.getBinding().getInInterceptors());
        chain.add(endpoint.getService().getInInterceptors());

        chain.doIntercept(message);        
    }
}
