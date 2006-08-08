package org.objectweb.celtix.interceptors;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.phase.PhaseInterceptorChain;

public class OutgoingChainInterceptor extends AbstractPhaseInterceptor<Message> {

    public OutgoingChainInterceptor() {
        super();
        setPhase(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) {
        Bus bus = (Bus) message.getExchange().get(Message.BUS);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getOutPhases());
        
        Endpoint ep = (Endpoint) message.getExchange().get(ExchangeConstants.ENDPOINT);
        chain.add(ep.getOutInterceptors());
        chain.add(ep.getService().getOutInterceptors());
        chain.add(bus.getOutInterceptors());
        
        chain.doIntercept(message);
    }
}
