package org.objectweb.celtix.bus.transports.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.objectweb.celtix.transports.jms.JMSAddressPolicyType;
import org.objectweb.celtix.transports.jms.JMSServerBehaviorPolicyType;


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
        JMSAddressPolicyType  addrDetails = transport.getJmsAddressDetails();
        JMSServerBehaviorPolicyType serverPolicy = null;
        if (transport instanceof JMSServerTransport) {
            serverPolicy = ((JMSServerTransport) transport).getJMSServerBehaviourPolicy();
        }

        // get JMS connection resources and destination
        //
        Context context = JMSUtils.getInitialContext(addrDetails);
        Connection connection = null;

        //TODO: Connection should use username and password from policy for Durable Subscriber.
        
        if (JMSConstants.JMS_QUEUE.equals(addrDetails.getDestinationStyle().value())) {
            QueueConnectionFactory qcf =
                (QueueConnectionFactory)context.lookup(addrDetails.getJndiConnectionFactoryName());
            connection = qcf.createQueueConnection();
        } else {
            TopicConnectionFactory tcf =
                (TopicConnectionFactory)context.lookup(addrDetails.getJndiConnectionFactoryName());
            connection = tcf.createTopicConnection();
//            //TODO: Need to add username from the policy once we decide on policy work.
//            // We will pull it from the security policy. Also need to think on this condition as we 
//            // have dropped the durableSubscriberName attribute from new schema. 
//            
//            if (addrDetails.getDurableSubscriberName() != null) {
//                String ext = transport instanceof JMSClientTransport ? "-client" : "-server";
//                connection.setClientID(System.getProperty("user.name" + ext));
//            }    
        }

        connection.start();

        Destination requestDestination = 
                (Destination) context.lookup(
                                           addrDetails.getJndiDestinationName());
        // UBHOLE: Need to decide on who creates the replyDestination. while seperating the policies
        // we need to decide on the precedence on values from config or wsdl.
        
        Destination replyDestination = (null != addrDetails.getJndiReplyDestinationName())
            ? (Destination) context.lookup(addrDetails.getJndiReplyDestinationName()) : null;

        // create session factory to manage session, reply destination,
        // producer and consumer pooling
        //
            
        JMSSessionFactory sf =
            new JMSSessionFactory(connection,
                                  replyDestination,
                                  addrDetails,
                                  serverPolicy);

        // notify transport that connection is complete
        //
        transport.connected(requestDestination, replyDestination, sf);
    }
}
