package org.objectweb.celtix.messaging;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.phase.PhaseManager;

public class ChainInitiationObserver implements MessageObserver {
    Endpoint endpoint;
    Bus bus;

    public ChainInitiationObserver(Endpoint endpoint, Bus bus) {
        super();
        this.endpoint = endpoint;
        this.bus = bus;
    }

    public void onMessage(Message message) {
        Exchange exchange = new ExchangeImpl();
        exchange.setInMessage(message);

        // setup chain
        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getExtension(PhaseManager.class)
            .getInPhases());
        chain.add(bus.getOutInterceptors());
        chain.add(endpoint.getOutInterceptors());
        chain.add(endpoint.getBinding().getOutInterceptors());
        chain.add(endpoint.getService().getOutInterceptors());

        chain.doIntercept(message);
    }
}
