package org.objectweb.celtix.interceptors;

import java.io.IOException;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;

/**
 * Takes the Conduit from the exchange and sends the message through it.
 *
 */
public class MessageSenderInterceptor extends AbstractPhaseInterceptor<Message> {

    public MessageSenderInterceptor() {
        super();
        setPhase(Phase.PREPARE_SEND);
    }

    public void handleMessage(Message message) {
        Conduit conduit = message.getConduit();
        if (conduit == null) {
            conduit = message.getExchange().getConduit();
        }
        
        try {
            conduit.send(message);
            
            message.getInterceptorChain().doIntercept(message);
            
            conduit.close(message);
        } catch (IOException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        }
    }
}
