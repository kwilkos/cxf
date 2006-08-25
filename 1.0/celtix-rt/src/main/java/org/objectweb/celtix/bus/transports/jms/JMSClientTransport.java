package org.objectweb.celtix.bus.transports.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.naming.NamingException;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.bus.management.counters.TransportClientCounters;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.jms.JMSClientBehaviorPolicyType;
import org.objectweb.celtix.transports.jms.context.JMSMessageHeadersType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;



public class JMSClientTransport extends JMSTransportBase implements ClientTransport {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JMSClientTransport.class);
    private static final long DEFAULT_RECEIVE_TIMEOUT = 0;
    
    protected boolean textPayload;
    TransportClientCounters counters;
    private JMSClientBehaviorPolicyType clientBehaviourPolicy;
    private ResponseCallback responseCallback;
    
    public JMSClientTransport(Bus bus, 
                              EndpointReferenceType address, 
                              ClientBinding binding) 
        throws WSDLException, IOException  {

        super(bus, address, false);
        clientBehaviourPolicy = getClientPolicy(configuration);
        counters = new TransportClientCounters("JMSClientTransport");
        
        EndpointReferenceUtils.setAddress(address, getAddrUriFromJMSAddrPolicy());
        targetEndpoint = address;
        
        textPayload = 
            JMSConstants.TEXT_MESSAGE_TYPE.equals(clientBehaviourPolicy.getMessageType().value());
        
        LOG.log(Level.FINE, "TEXT_MESSAGE_TYPE: " , textPayload);
        LOG.log(Level.FINE, "QUEUE_DESTINATION_STYLE: " , queueDestinationStyle);
        if (binding != null) {
            responseCallback = binding.createResponseCallback();
        }
        entry("JMSClientTransport Constructor");
    }

    private JMSClientBehaviorPolicyType getClientPolicy(Configuration conf) {
        JMSClientBehaviorPolicyType pol = conf.getObject(JMSClientBehaviorPolicyType.class, "jmsClient");
        if (pol == null) {
            pol = new JMSClientBehaviorPolicyType();
        }
        return pol;
    }
    
    public JMSClientBehaviorPolicyType getJMSClientBehaviourPolicy() {
        
        return clientBehaviourPolicy;
    }
    
    //TODO: Revisit for proper implementation and changes if any.

    public void shutdown() {
        entry("JMSClientTransport shutdown()");

        // ensure resources held by session factory are released
        //
        if (sessionFactory != null) {
            sessionFactory.shutdown();
        }
    }
    
    public EndpointReferenceType getTargetEndpoint() {
        return targetEndpoint;
    }
    
    public EndpointReferenceType getDecoupledEndpoint() throws IOException {
        
        if (jmsAddressPolicy.getJndiReplyDestinationName() != null) {
            EndpointReferenceType epr = new EndpointReferenceType();
            EndpointReferenceUtils.setAddress(epr, getReplyTotAddrUriFromJMSAddrPolicy());
            return epr;
        }
        
        return null;
    }
    
    public Port getPort() {
        return port;
    }
    
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        return new JMSOutputStreamContext(context);
    }


    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
    }

    public InputStreamMessageContext invoke(OutputStreamMessageContext context)
        throws IOException {
        
        if (!queueDestinationStyle) {
            LOG.log(Level.WARNING, "Non-oneway invocations not supported for JMS Topics");
            throw new IOException("Non-oneway invocations not supported for JMS Topics");
        }

        try {
            byte[] responseData = null;
            if (textPayload) {
                String responseString = (String)invoke(context, true);
                responseData = responseString.getBytes();
            } else {
                responseData = (byte[])invoke(context, true);
            }
            counters.getInvoke().increase();
            JMSInputStreamContext respContext = 
                new JMSInputStreamContext(new ByteArrayInputStream(responseData));
            
            if (context.containsKey(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS)) {
                JMSMessageHeadersType responseHdr = 
                    (JMSMessageHeadersType)context.remove(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS);
                respContext.put(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS, responseHdr);
                respContext.setScope(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS, 
                                     MessageContext.Scope.APPLICATION);
            }
                        
            return respContext;
        } catch (Exception ex) {
            //TODO: decide what to do with the exception.
            counters.getInvokeError().increase();
            throw new IOException(ex.getMessage());
        }
    }


    /**
     * Variant on invoke used for oneways.
     *
     * @param request the buffer to send
     */
    public void invokeOneway(OutputStreamMessageContext context) throws IOException {
        try {
            invoke(context, false);
            counters.getInvokeOneWay();
        } catch (Exception ex) {
            counters.getInvokeError().increase();
            throw new IOException(ex.getMessage());
        }
    }

    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context,
                                                         Executor executor)
        throws IOException {
        return null;
    }

    public ResponseCallback getResponseCallback() {
        return responseCallback;
    }

    /**
     * Internal invoke mechanics.
     *
     * @param request the buffer to send
     * @param responseExpected true iff a response is expected
     * @return the response buffer if expected
     */
    private Object invoke(OutputStreamMessageContext context, boolean responseExpected)
        throws JMSException, NamingException {
        entry("JMSClientTransport invoke()");

        try {
            if (null == sessionFactory) {
                JMSProviderHub.connect(this);
            }
        } catch (JMSException ex) {
            LOG.log(Level.FINE, "JMS connect failed with JMSException : ", ex);
            throw ex;
        } catch (NamingException e) {
            LOG.log(Level.FINE, "JMS connect failed with NamingException : ", e);
            throw e;
        }

        if (sessionFactory == null) {
            throw new java.lang.IllegalStateException("JMSClientTransport not connected");
        }

        PooledSession pooledSession = sessionFactory.get(responseExpected);
        send(pooledSession, context, responseExpected);

        Object response = null;

        if (responseExpected) {
            response = receive(pooledSession, context);
        }

        sessionFactory.recycle(pooledSession);

        return response;
    }


    /**
     * Send mechanics.
     *
     * @param request the request buffer
     * @param pooledSession the shared JMS resources
     */
    private void send(PooledSession pooledSession,
                              OutputStreamMessageContext context,
                              boolean responseExpected)
        throws JMSException {
        Object request;

        if (textPayload) {
            request = context.getOutputStream().toString();
        } else {
            request = ((ByteArrayOutputStream)context.getOutputStream()).toByteArray();
        }

        Destination replyTo = pooledSession.destination();

        //We don't want to send temp queue in
        //replyTo header for oneway calls
        if (!responseExpected
            && (jmsAddressPolicy.getJndiReplyDestinationName() == null)) {
            replyTo = null;
        }

        Message message = marshal(request, pooledSession.session(), replyTo, 
                                  clientBehaviourPolicy.getMessageType().value());
      //  message.get

        JMSMessageHeadersType headers =
            (JMSMessageHeadersType)context.get(JMSConstants.JMS_CLIENT_REQUEST_HEADERS);


        int deliveryMode = getJMSDeliveryMode(headers);
        int priority = getJMSPriority(headers);
        String correlationID = getCorrelationId(headers);
        long ttl = getTimeToLive(headers);
        if (ttl <= 0) {
            ttl = DEFAULT_RECEIVE_TIMEOUT;
        }
        
        setMessageProperties(headers, message);
        if (responseExpected) {
            String id = pooledSession.getCorrelationID();

            if (id != null) {
                if (correlationID != null) {
                    String error = "User cannot set JMSCorrelationID when "
                        + "making a request/reply invocation using "
                        + "a static replyTo Queue.";
                    throw new JMSException(error);
                }
                correlationID = id;
            }
        }

        if (correlationID != null) {
            message.setJMSCorrelationID(correlationID);
        } else {
            //No message correlation id is set. Whatever comeback will be accepted as responses.
            // We assume that it will only happen in case of the temp. reply queue.
        }

        LOG.log(Level.FINE, "client sending request: ",  message);

        if (queueDestinationStyle) {
            QueueSender sender = (QueueSender)pooledSession.producer();
            sender.setTimeToLive(ttl);
            sender.send((Queue)targetDestination, message, deliveryMode, priority, ttl);
        } else {
            TopicPublisher publisher = (TopicPublisher)pooledSession.producer();
            publisher.setTimeToLive(ttl);
            publisher.publish((Topic)targetDestination, message, deliveryMode, priority, ttl);
        }
    }


    /**
     * Receive mechanics.
     *
     * @param pooledSession the shared JMS resources
     * @retrun the response buffer
     */
    private Object receive(PooledSession pooledSession,
                           OutputStreamMessageContext context)
        throws JMSException {
        Object response = null;
        
        long timeout = DEFAULT_RECEIVE_TIMEOUT;

        Long receiveTimeout = (Long)context.get(JMSConstants.JMS_CLIENT_RECEIVE_TIMEOUT);

        if (receiveTimeout != null) {
            timeout = receiveTimeout.longValue();
        }
        
        Message message = pooledSession.consumer().receive(timeout);
        LOG.log(Level.FINE, "client received reply: " , message);

        if (message != null) {
            
            populateIncomingContext(message, context, JMSConstants.JMS_CLIENT_RESPONSE_HEADERS);
            String messageType = message instanceof TextMessage 
                        ? JMSConstants.TEXT_MESSAGE_TYPE : JMSConstants.BINARY_MESSAGE_TYPE;
            response = unmarshal(message, messageType);
            return response;
        } else {
            String error = "JMSClientTransport.receive() timed out. No message available.";
            LOG.log(Level.SEVERE, error);
            //TODO: Review what exception should we throw.
            //throw new JMSException(error);
            return null;
        }
    }
}
