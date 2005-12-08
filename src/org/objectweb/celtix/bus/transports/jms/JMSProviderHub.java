package org.objectweb.celtix.bus.transports.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.objectweb.celtix.transports.jms.AddressType;


/**
 * This class acts as the hub of JMS provider usage, creating shared
 * JMS Connections and providing access to a pool of JMS Sessions.
 * <p>
 * A new JMS connection is created for each each port based
 * <jms:address> - however its likely that in practice the same JMS
 * provider will be specified for each port, and hence the connection
 * resources could be shared accross ports.
 * <p>
 * For the moment this class is realized as just a container for
 * static methods, but the intention is to support in future sharing
 * of JMS resources accross compatible ports.
 *
 * @author Eoghan Glynn
 */
public final class JMSProviderHub {
    

    /**
     * Constructor.
     */
    private JMSProviderHub() {
    }

    //--java.lang.Object Overrides----------------------------------------------
    public String toString() {
        return "JMSProviderHub";
    }


    //--Methods-----------------------------------------------------------------

    protected static void connect(JMSTransportBase transport) throws JMSException, NamingException {
        AddressType  addrDetails = transport.getJmsAddressDetails();

        // get JMS connection resources and destination
        //
        Context context = JMSUtils.getInitialContext(addrDetails);
        Connection connection = null;

        //TODO: Connection can use username and password from policy for Durable Subscriber.
        
        if (JMSConstants.JMS_QUEUE.equals(addrDetails.getDestinationStyle().value())) {
            QueueConnectionFactory qcf =
                (QueueConnectionFactory)context.lookup(addrDetails.getJndiConnectionFactoryName());
            connection = qcf.createQueueConnection();
        } else {
            TopicConnectionFactory tcf =
                (TopicConnectionFactory)context.lookup(addrDetails.getJndiConnectionFactoryName());
            connection = tcf.createTopicConnection();
            //TODO: Need to add username from the policy once we decide on policy work.
            if (addrDetails.getDurableSubscriberName() != null) {
                String ext = transport instanceof JMSClientTransport ? "-client" : "-server";
                connection.setClientID(System.getProperty("user.name" + ext));    
            }    
        }

        connection.start();

        Destination destination = (Destination) context.lookup(addrDetails.getJndiDestinationName());

        // create session factory to manage session, reply destination,
        // producer and consumer pooling
        //
        String destinationStyle = addrDetails.getDestinationStyle().value();
        JMSSessionFactory sf =
            new JMSSessionFactory(connection,
                                  JMSConstants.JMS_QUEUE.equals(destinationStyle),
                                  addrDetails.getMessageSelector(),
                                  addrDetails.getDurableSubscriberName());

        // notify transport that connection is complete
        //
        transport.connected(destination, sf);
    }
}
