package org.objectweb.celtix.interceptors;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.phase.PhaseManager;

public class OutgoingChainInterceptor extends AbstractPhaseInterceptor<Message> {

    public OutgoingChainInterceptor() {
        super();
        setPhase(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) {
        Exchange ex = message.getExchange();
        Bus bus = (Bus)ex.get(Message.BUS);
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
        
        Endpoint ep = (Endpoint)ex.get(ExchangeConstants.ENDPOINT);
        chain.add(ep.getOutInterceptors());
        chain.add(ep.getService().getOutInterceptors());
        if (ep.getBinding() != null) {
            chain.add(ep.getBinding().getOutInterceptors());
        }
        chain.add(bus.getOutInterceptors());        
        
        Message outMessage = message.getExchange().getOutMessage();
        if (outMessage.getConduit() == null
            && ex.getConduit() == null
            && ex.getDestination() != null) {
            try {
                ex.setConduit(ex.getDestination().getBackChannel(message, null, null));
            } catch (WSDLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        outMessage.setInterceptorChain(chain);
        chain.doIntercept(outMessage);
    }
}
