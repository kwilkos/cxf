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
        setPhase(Phase.SEND);
    }

    public void handleMessage(Message message) {
        Conduit conduit = message.getExchange().getConduit();
        
        try {
            conduit.send(message);
        } catch (IOException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        }
    }
}
