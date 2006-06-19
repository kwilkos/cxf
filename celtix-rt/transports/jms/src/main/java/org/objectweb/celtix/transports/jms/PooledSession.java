package org.objectweb.celtix.transports.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

/**
 * Encapsulates pooled session, unidentified producer, destination &
 * associated consumer (certain elements may be null depending on the
 * context).
 * <p>
 * Currently only the point-to-point domain is supported,
 * though the intention is to genericize this to the pub-sub domain
 * also.
 *
 * @author Eoghan Glynn
 */
public class PooledSession {
    private final Session theSession;
    private Destination theDestination;
    private final MessageProducer theProducer;
    private MessageConsumer theConsumer;

    private String correlationID;

    /**
     * Constructor.
     */
    PooledSession(Session session,
                  Destination destination,
                  MessageProducer producer,
                  MessageConsumer consumer) {
        theSession = session;
        theDestination = destination;
        theProducer = producer;
        theConsumer = consumer;
    }
    

    /**
     * @return the pooled JMS Session
     */
    Session session() {
        return theSession;
    }


    /**
     * @return the destination associated with the consumer
     */
    Destination destination() {
        return theDestination;
    }


    /**
     * @param destination the destination to encapsulate
     */
    void destination(Destination destination) {
        theDestination = destination;
    }


    /**
     * @return the unidentified producer
     */
    MessageProducer producer() {
        return theProducer;
    }


    /**
     * @return the per-destination consumer
     */
    MessageConsumer consumer() {
        return theConsumer;
    }

    /**
     * @return messageSelector if any set.
     */
    
    String getCorrelationID() throws JMSException {        
        if (correlationID == null && theConsumer != null) {
            //Must be request/reply
            String selector = theConsumer.getMessageSelector();
            
            if (selector != null && selector.startsWith("JMSCorrelationID")) {
                int i = selector.indexOf('\'');
                correlationID = selector.substring(i + 1, selector.length() - 1);
            }       
        }
        
        return correlationID;
    }

    /**
     * @param consumer the consumer to encapsulate
     */
    void consumer(MessageConsumer consumer) {
        theConsumer = consumer;
    }


    void close() throws JMSException {
        if (theProducer != null) {
            theProducer.close();
        }

        if (theConsumer != null) {
            theConsumer.close();
        }

        if (theDestination instanceof TemporaryQueue) {
            ((TemporaryQueue)theDestination).delete();
        }

        if (theSession != null) {
            theSession.close();
        }
    }
}
