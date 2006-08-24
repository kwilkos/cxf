package org.objectweb.celtix.transports.jms;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;


import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;


import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.transports.jms.context.JMSMessageHeadersType;
import org.objectweb.celtix.transports.jms.context.JMSPropertyType;
import org.objectweb.celtix.transports.jms.jms_conf.JMSSessionPoolConfigPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class JMSConduit implements Conduit {
    static final String JMS_CONNECTION = "jms.connection";
    private static final Logger LOG = LogUtils.getL7dLogger(JMSConduit.class);
    
    protected boolean queueDestinationStyle;
    protected Destination targetDestination;
    protected Destination replyDestination;
    protected JMSSessionFactory sessionFactory;
    protected JMSSessionPoolConfigPolicy sessionPoolConfig;    
    protected JMSConduitConfiguration configuration;
    private JMSAddressPolicyType jmsAddressPolicy;
       
    //private final Bus bus;
    private MessageObserver incomingObserver;
    private EndpointReferenceType target;
    private boolean isServer;
    
    //NOTE need to define the JMSConduit to be server or client
    public JMSConduit(Bus b, EndpointInfo endpointInfo) {
        this(b, endpointInfo, null);
    }
    
    public JMSConduit(Bus b,
                      EndpointInfo endpointInfo,
                      EndpointReferenceType target) {
        this(b, endpointInfo, target, 
             new JMSConduitConfiguration(b, endpointInfo));
    }
    
    public JMSConduit(Bus b,
                      EndpointInfo endpointInfo,
                      EndpointReferenceType target,
                      JMSConduitConfiguration conf) {
        //bus = b;
        /*port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), epr);*/
        configuration = conf;          
        queueDestinationStyle =
            JMSConstants.JMS_QUEUE.equals(jmsAddressPolicy.getDestinationStyle().value());
        jmsAddressPolicy = conf.getJmsAddressDetails();
        
    }
    
    
    

    public void send(Message message) throws IOException {
        // TODO Auto-generated method stub 
        
    }

    public void close(Message message) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public EndpointReferenceType getTarget() {
        return target;
    }

    public Destination getBackChannel() {
        // TODO Auto-generated method stub
        return null;
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }

    public void setMessageObserver(MessageObserver observer) {
        incomingObserver = observer;
        LOG.info("registering incoming observer: " + incomingObserver);        
    }
     
    public boolean isServer() {
        return isServer;
    }
   
        
    public final JMSAddressPolicyType  getJmsAddressDetails() {
        return jmsAddressPolicy;
    }
    
   
    /**
     * Callback from the JMSProviderHub indicating the ClientTransport has
     * been sucessfully connected.
     *
     * @param targetDestination the target destination
     * @param sessionFactory used to get access to a pooled JMS resources
     */
    protected void connected(Destination dest,
                             Destination reply, 
                             JMSSessionFactory factory) {
        targetDestination = dest;
        replyDestination = reply;
        sessionFactory = factory;
    }


    /**
     * Create a JMS of the appropriate type populated with the given payload.
     *
     * @param payload the message payload, expected to be either of type
     * String or byte[] depending on payload type
     * @param session the JMS session
     * @param replyTo the ReplyTo destination if any
     * @return a JMS of the appropriate type populated with the given payload
     */
    protected javax.jms.Message marshal(Object payload, Session session, Destination replyTo,
                              String messageType) throws JMSException {
        javax.jms.Message message = null;

        if (JMSConstants.TEXT_MESSAGE_TYPE.equals(messageType)) {
            message = session.createTextMessage((String)payload);
        } else {
            message = session.createObjectMessage();
            ((ObjectMessage)message).setObject((byte[])payload);
        }

        if (replyTo != null) {
            message.setJMSReplyTo((javax.jms.Destination)replyTo);
        }

        return message;
    }


    /**
     * Unmarshal the payload of an incoming message.
     *
     * @param message the incoming message
     * @return the unmarshalled message payload, either of type String or
     * byte[] depending on payload type
     */
    protected Object unmarshal(javax.jms.Message message, String messageType) throws JMSException {
        Object ret = null;

        if (JMSConstants.TEXT_MESSAGE_TYPE.equals(messageType)) {
            ret = ((TextMessage)message).getText();
        } else {
            ret = (byte[])((ObjectMessage)message).getObject();
        }

        return ret;
    }

    protected JMSMessageHeadersType populateIncomingContext(javax.jms.Message message,
                                                     MessageContext context,
                                                     String headerType)  throws JMSException {
        JMSMessageHeadersType headers = null;

        headers = (JMSMessageHeadersType)context.get(headerType);

        if (headers == null) {
            headers = new JMSMessageHeadersType();
            context.put(headerType, headers);
        }

        headers.setJMSCorrelationID(message.getJMSCorrelationID());
        headers.setJMSDeliveryMode(message.getJMSDeliveryMode());
        headers.setJMSExpiration(new Long(message.getJMSExpiration()));
        headers.setJMSMessageID(message.getJMSMessageID());
        headers.setJMSPriority(message.getJMSPriority());
        headers.setJMSRedelivered(Boolean.valueOf(message.getJMSRedelivered()));
        headers.setJMSTimeStamp(new Long(message.getJMSTimestamp()));
        headers.setJMSType(message.getJMSType());

        List<JMSPropertyType> props = headers.getProperty();
        Enumeration enm = message.getPropertyNames();
        while (enm.hasMoreElements()) {
            String name = (String)enm.nextElement();
            String val = message.getStringProperty(name);
            JMSPropertyType prop = new JMSPropertyType();
            prop.setName(name);
            prop.setValue(val);
            props.add(prop);
        }

        return headers;
    }

    protected int getJMSDeliveryMode(JMSMessageHeadersType headers) {
        int deliveryMode = javax.jms.Message.DEFAULT_DELIVERY_MODE;

        if (headers != null && headers.isSetJMSDeliveryMode()) {
            deliveryMode = headers.getJMSDeliveryMode();
        }
        return deliveryMode;
    }

    protected int getJMSPriority(JMSMessageHeadersType headers) {
        int priority = javax.jms.Message.DEFAULT_PRIORITY;
        if (headers != null && headers.isSetJMSPriority()) {
            priority = headers.getJMSPriority();
        }
        return priority;
    }

    protected long getTimeToLive(JMSMessageHeadersType headers) {
        long ttl = -1;
        if (headers != null && headers.isSetTimeToLive()) {
            ttl = headers.getTimeToLive();
        }
        return ttl;
    }

    protected String getCorrelationId(JMSMessageHeadersType headers) {
        String correlationId  = null;
        if (headers != null
            && headers.isSetJMSCorrelationID()) {
            correlationId = headers.getJMSCorrelationID();
        }
        return correlationId;
    }

    protected void setMessageProperties(JMSMessageHeadersType headers, javax.jms.Message message)
        throws JMSException {

        if (headers != null
                && headers.isSetProperty()) {
            List<JMSPropertyType> props = headers.getProperty();
            for (int x = 0; x < props.size(); x++) {
                message.setStringProperty(props.get(x).getName(), props.get(x).getValue());
            }
        }
    }
    
    protected String getAddrUriFromJMSAddrPolicy() {
        return "jms:" 
                        + jmsAddressPolicy.getJndiConnectionFactoryName() 
                        + "#"
                        + jmsAddressPolicy.getJndiDestinationName();
    }
    
    protected String getReplyTotAddrUriFromJMSAddrPolicy() {
        return "jms:" 
                        + jmsAddressPolicy.getJndiConnectionFactoryName() 
                        + "#"
                        + jmsAddressPolicy.getJndiReplyDestinationName();
    }

 

}
