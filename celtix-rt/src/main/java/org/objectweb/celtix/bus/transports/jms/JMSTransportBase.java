package org.objectweb.celtix.bus.transports.jms;

import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import  javax.jms.ObjectMessage;
import  javax.jms.Session;
import  javax.jms.TextMessage;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.handler.MessageContext;



import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.transports.jms.AddressType;
import org.objectweb.celtix.transports.jms.context.JMSClientHeadersType;
import org.objectweb.celtix.transports.jms.context.JMSHeadersType;
import org.objectweb.celtix.transports.jms.context.JMSPropertyType;
import org.objectweb.celtix.transports.jms.context.JMSServerHeadersType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class JMSTransportBase {
    //--Member Variables--------------------------------------------------------
    private static final Logger LOG = LogUtils.getL7dLogger(JMSTransportBase.class);
    protected AddressType jmsAddressDetails;
    protected boolean textPayload;
    protected boolean queueDestinationStyle;
    protected Destination targetDestination;
    protected Destination replyDestination;
    protected JMSSessionFactory sessionFactory;
    protected Bus theBus;
    protected EndpointReferenceType targetEndpoint;    
    protected Port port;
    
    //--Constructors------------------------------------------------------------
    public JMSTransportBase(Bus bus, EndpointReferenceType epr) throws WSDLException {
        theBus = bus;
        port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), epr);
        List<?> list = port.getExtensibilityElements();
        for (Object ep : list) {
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if (ext instanceof AddressType) {
                jmsAddressDetails = (AddressType)ext;
            }
        }
        textPayload = 
            JMSConstants.TEXT_MESSAGE_TYPE.equals(jmsAddressDetails.getMessageType().value());
        queueDestinationStyle = 
            JMSConstants.JMS_QUEUE.equals(jmsAddressDetails.getDestinationStyle().value());
        LOG.log(Level.FINE, "QUEUE_DESTINATION_STYLE: " + queueDestinationStyle);
        targetEndpoint = epr;
    }
    
    
    //--Methods-----------------------------------------------------------------

    public final AddressType  getJmsAddressDetails() {
        return jmsAddressDetails;
    }
    /**
     * Callback from the JMSProviderHub indicating the ClientTransport has
     * been sucessfully connected.
     *
     * @param targetDestination the target destination
     * @param sessionFactory used to get access to a pooled JMS resources
     */
    protected void connected(Destination target, Destination reply, JMSSessionFactory factory) {
        targetDestination = target;
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
    protected Message marshal(Object payload, Session session, Destination replyTo) throws JMSException {
        Message message = null;

        if (textPayload) {
            message = session.createTextMessage((String) payload);
        } else {
            message = session.createObjectMessage();
            ((ObjectMessage) message).setObject((byte[])payload);
        }

        if (replyTo != null) {
            message.setJMSReplyTo(replyTo);
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
    protected Object unmarshal(Message message) throws JMSException {
        Object ret = null;

        if (textPayload) {
            ret = ((TextMessage) message).getText();
        } else {
            ret = (byte[]) ((ObjectMessage) message).getObject();
        }

        return ret;
    }


    protected final void entry(String trace) {
        LOG.log(Level.FINE, trace);
    }
    
    protected JMSHeadersType populateIncomingContext(Message message, 
                                                             MessageContext context, 
                                                             boolean isServer)  throws JMSException {
        JMSHeadersType headers = null;
        if (isServer) {
            headers = (JMSServerHeadersType) context.get(JMSConstants.JMS_RESPONSE_HEADERS);
            if (headers == null) {
                headers = new JMSServerHeadersType();
            }
        } else {
            headers = (JMSClientHeadersType) context.get(JMSConstants.JMS_REQUEST_HEADERS);
            if (headers == null) {
                headers = new JMSClientHeadersType();
            }
        }
        
        headers.setJMSCorrelationID(message.getJMSCorrelationID());
        headers.setJMSDeliveryMode(new Integer(message.getJMSDeliveryMode()));
        headers.setJMSExpiration(new Long(message.getJMSExpiration()));
        headers.setJMSMessageID(message.getJMSMessageID());
        headers.setJMSPriority(new Integer(message.getJMSPriority()));
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
    
    protected int getJMSDeliveryMode(JMSHeadersType headers) {
        int deliveryMode = Message.DEFAULT_DELIVERY_MODE;
        
        if (headers != null  
                && headers.getJMSDeliveryMode() != null) {
            deliveryMode = headers.getJMSDeliveryMode().intValue();
        }
        return deliveryMode;
    }
    
    protected int getJMSPriority(JMSHeadersType headers) {
        int priority = Message.DEFAULT_PRIORITY;
        
        if (headers != null  
                && headers.getJMSPriority() != null) {
            priority = headers.getJMSPriority().intValue();
        }        
        return priority;
    }
    
    protected long getTimeToLive(JMSHeadersType headers) {
        long ttl = Message.DEFAULT_TIME_TO_LIVE;
        
        if (headers != null  
                && headers.getTimeToLive() != null) {
            ttl = headers.getTimeToLive().longValue();
        }        
        return ttl;
    }
    
    protected String getCorrelationId(JMSHeadersType headers) {
        String correlationId  = null;
        if (headers != null  
            && headers.getJMSCorrelationID() != null) {
            correlationId = headers.getJMSCorrelationID();
        }        
        return correlationId;
    }
    
    protected void setMessageProperties(JMSHeadersType headers, Message message) 
        throws JMSException {
        
        if (headers != null  
                && headers.getProperty() != null) {
            List<JMSPropertyType> props = headers.getProperty();
            for (int x = 0; x < props.size(); x++) {
                message.setStringProperty(props.get(x).getName(), props.get(x).getValue());
            }
        }        
    }
}
