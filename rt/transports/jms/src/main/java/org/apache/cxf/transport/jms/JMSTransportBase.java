/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.jms;

import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.jms.JMSAddressPolicyType;
import org.apache.cxf.transports.jms.context.JMSMessageHeadersType;
import org.apache.cxf.transports.jms.context.JMSPropertyType;


public class JMSTransportBase {    
    
    protected boolean queueDestinationStyle;
    protected Destination targetDestination;
    protected Destination replyDestination;
    protected JMSSessionFactory sessionFactory;    
    protected JMSAddressPolicyType jmsAddressPolicy;
    protected Bus bus;
    protected JMSConfiguration jmsConf;
    //protected EndpointReferenceType targetEndpoint;
    protected EndpointInfo endpointInfo;
    
    
    //--Constructors------------------------------------------------------------
    public JMSTransportBase(Bus b, EndpointInfo endpoint, boolean isServer, JMSConfiguration conf) {
        bus = b;
        jmsConf = conf;
        jmsAddressPolicy = jmsConf.getJmsAddressDetails();
        //sessionPoolConfig = getSessionPoolPolicy(configuration);
        endpointInfo = endpoint;
        queueDestinationStyle =
            JMSConstants.JMS_QUEUE.equals(jmsAddressPolicy.getDestinationStyle().value());
    }

    public final JMSAddressPolicyType  getJmsAddressDetails() {
        return jmsAddressPolicy;
    }
    
    public final JMSConfiguration getJMSConfiguration() {
        return jmsConf;
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
    protected Message marshal(Object payload, Session session, Destination replyTo,
                              String messageType) throws JMSException {
        Message message = null;

        if (JMSConstants.TEXT_MESSAGE_TYPE.equals(messageType)) {
            message = session.createTextMessage((String)payload);
        } else {
            message = session.createObjectMessage();
            ((ObjectMessage)message).setObject((byte[])payload);
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
    protected Object unmarshal(Message message, String messageType) throws JMSException {
        Object ret = null;

        if (JMSConstants.TEXT_MESSAGE_TYPE.equals(messageType)) {
            ret = ((TextMessage)message).getText();
        } else {
            ret = (byte[])((ObjectMessage)message).getObject();
        }

        return ret;
    }

    protected JMSMessageHeadersType populateIncomingContext(javax.jms.Message message,
                                                            org.apache.cxf.message.Message inMessage,
                                                     String headerType)  throws JMSException {
        JMSMessageHeadersType headers = null;

        headers = (JMSMessageHeadersType)inMessage.get(headerType);

        if (headers == null) {           
            headers = new JMSMessageHeadersType();
            inMessage.put(headerType, headers);
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

    protected int getJMSDeliveryMode(JMSMessageHeadersType headers) {
        int deliveryMode = Message.DEFAULT_DELIVERY_MODE;

        if (headers != null && headers.isSetJMSDeliveryMode()) {
            deliveryMode = headers.getJMSDeliveryMode();
        }
        return deliveryMode;
    }

    protected int getJMSPriority(JMSMessageHeadersType headers) {
        int priority = Message.DEFAULT_PRIORITY;
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

    protected void setMessageProperties(JMSMessageHeadersType headers, Message message)
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
