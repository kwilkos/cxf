package org.objectweb.celtix.bus.transports.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.jms.JMSServerBehaviorPolicyType;
import org.objectweb.celtix.transports.jms.context.JMSServerHeadersType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


public class JMSServerTransport extends JMSTransportBase implements ServerTransport {
    static final Logger LOG = LogUtils.getL7dLogger(JMSServerTransport.class);
    private static final String JMS_SERVER_TRANSPORT_MESSAGE =
        JMSServerTransport.class.getName() + ".IncomingMessage";
    private static final String JMS_SERVER_TRANSPORT_CORRELATION_ID =
        JMSServerTransport.class.getName() + ".CorrelationID";

    ServerTransportCallback callback;
    private PooledSession listenerSession;
    private Thread listenerThread;
    private JMSServerBehaviorPolicyType serverBehaviourPolicy;


    public JMSServerTransport(Bus bus, EndpointReferenceType address)
        throws WSDLException {
        super(bus, address, true);
        serverBehaviourPolicy = getServerPolicy(configuration); 
        entry("JMSServerTransport Constructor");
    }
    
    private JMSServerBehaviorPolicyType getServerPolicy(Configuration conf) {
        JMSServerBehaviorPolicyType pol = conf.getObject(JMSServerBehaviorPolicyType.class, "jmsServer");
        if (pol == null) {
            pol = new JMSServerBehaviorPolicyType();
        }
        return pol;
    }
    
    public JMSServerBehaviorPolicyType getJMSServerBehaviourPolicy() {
        return serverBehaviourPolicy;
    }

    public void activate(ServerTransportCallback transportCB) throws IOException {
        entry("JMSServerTransport activate().... ");
        callback = transportCB;

        try {
            LOG.log(Level.FINE, "establishing JMS connection");
            JMSProviderHub.connect(this);

            //Get a non-pooled session.
            listenerSession = sessionFactory.get(targetDestination);
            listenerThread = new JMSListenerThread(listenerSession, this);
            listenerThread.start();
        } catch (JMSException ex) {
            LOG.log(Level.FINE, "JMS connect failed with JMSException : ", ex);
            throw new IOException(ex.getMessage());
        } catch (NamingException nex) {
            LOG.log(Level.FINE, "JMS connect failed with NamingException : ", nex);
            throw new IOException(nex.getMessage());
        }
    }
    
    public OutputStreamMessageContext rebase(MessageContext context,
                                             EndpointReferenceType decoupledResponseEndpoint)
        throws IOException {
        return null;
    }

    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        //ubhole: This should also put the JMS Response headers info. 
        // This should come from configuation/context. 
        // Info. will include the JMSMessage->timeToLive and other relevant info. (if any)
        return new JMSOutputStreamContext(context);
    }

    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
    }

    public void deactivate() throws IOException {
        try {
            listenerSession.consumer().close();
            if (listenerThread != null) {
                listenerThread.join();
            }
            sessionFactory.shutdown();
        } catch (InterruptedException e) {
            //Don't do anything...
        } catch (JMSException ex) {
            //
        }
        //throw new IOException("deactivate() is not implemented for JMSServerTransport.");
    }

    public void shutdown() {
        entry("JMSServerTransport shutdown()");
        try {
            this.deactivate();
        } catch (IOException ex) {
            // Ignore for now.
        }
    }

    public void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context)
        throws IOException {

        Message message = (Message) bindingContext.get(JMS_SERVER_TRANSPORT_MESSAGE);
        String correlationID = (String) bindingContext.get(JMS_SERVER_TRANSPORT_CORRELATION_ID);
        PooledSession replySession = null;
         // ensure non-oneways in point-to-point domain

        if (!context.isOneWay()) {
            if (queueDestinationStyle) {
                try {
//                  send reply
                    Queue replyTo = (null != message.getJMSReplyTo()) 
                        ? (Queue)message.getJMSReplyTo() : (Queue)replyDestination;
                    replySession = sessionFactory.get(false);

                    Message reply;
                    boolean textPayload = message instanceof TextMessage 
                                    ? true : false;
                    if (textPayload) {
                        reply = marshal(context.getOutputStream().toString(), 
                                                replySession.session(), 
                                                null, 
                                                JMSConstants.TEXT_MESSAGE_TYPE);
                    } else {
                        reply = marshal(((ByteArrayOutputStream) context.getOutputStream()).toByteArray(),
                                               replySession.session(),
                                               null, 
                                               JMSConstants.BINARY_MESSAGE_TYPE);
                    }

                    if (correlationID != null && !"".equals(correlationID)) {
                        reply.setJMSCorrelationID(correlationID);
                    }

                    QueueSender sender = (QueueSender)replySession.producer();

                    JMSServerHeadersType headers =
                        (JMSServerHeadersType) context.get(JMSConstants.JMS_RESPONSE_HEADERS);

                    int deliveryMode = getJMSDeliveryMode(headers);
                    int priority = getJMSPriority(headers);
                    long ttl = getTimeToLive(headers);

                    setMessageProperties(headers, message);

                    LOG.log(Level.FINE, "server sending reply: ", reply);

                    TimeZone tz = new SimpleTimeZone(0, "GMT");
                    Calendar cal = new GregorianCalendar(tz);
                    long timeToLive =  message.getJMSExpiration() - cal.getTimeInMillis();
                    if (timeToLive > 0) {
                        reply.setJMSExpiration(timeToLive);
                        ttl = timeToLive;
                        sender.send(replyTo, reply, deliveryMode, priority, ttl);
                    } else {
                        LOG.log(Level.INFO, "Message time to live is already expired skipping response.");
                    }                    
                } catch (JMSException ex) {
                    LOG.log(Level.WARNING, "Failed in post dispatch ...", ex);
                    throw new IOException(ex.getMessage());
                } finally {
                    // house-keeping
                    if (replySession != null) {
                        sessionFactory.recycle(replySession);
                    }
                }
            } else {
                // we will never receive a non-oneway invocation in pub-sub
                // domain from Celtix client - however a mis-behaving pure JMS
                // client could conceivably make suce an invocation, in which
                // case we silently discard the reply
                LOG.log(Level.WARNING,
                                             "discarding reply for non-oneway invocation ",
                                              "with 'topic' destinationStyle");
            }
        }
    }


    /**
     * Helper method to process incoming message.
     *
     * @param message the incoming message
     */
    protected void incoming(Message message) throws IOException {
        try {
            LOG.log(Level.FINE, "server received request: ", message);
            String correlationID = message.getJMSCorrelationID();

            if (correlationID == null
                || "".equals(correlationID)
                && serverBehaviourPolicy.isUseMessageIDAsCorrelationID()) {
                correlationID = message.getJMSMessageID();
            }

            String msgType = message instanceof TextMessage 
                    ? JMSConstants.TEXT_MESSAGE_TYPE : JMSConstants.BINARY_MESSAGE_TYPE;
            Object request = unmarshal(message, msgType);

            byte[] bytes = null;

            if (JMSConstants.TEXT_MESSAGE_TYPE.equals(msgType)) {
                String requestString = (String)request;
                LOG.log(Level.FINE, "server received request: ", requestString);
                bytes = requestString.getBytes();
            } else {
                bytes = (byte[])request;
            }

            JMSInputStreamContext context = new JMSInputStreamContext(new ByteArrayInputStream(bytes));
            populateIncomingContext(message, context, true);


            context.put(JMS_SERVER_TRANSPORT_MESSAGE, message);
            context.put(JMS_SERVER_TRANSPORT_CORRELATION_ID, correlationID);
            callback.dispatch(context, this);

        } catch (JMSException jmsex) {
            //TODO: need to revisit for which exception should we throw.
            throw new IOException(jmsex.getMessage());
        } 
    }

    class JMSListenerThread extends Thread {
        final JMSServerTransport theTransport;
        private final PooledSession listenSession;

        public JMSListenerThread(PooledSession session,
                                 JMSServerTransport transport) {
            listenSession = session;
            theTransport = transport;
        }

        public void run() {
            try {
                while (true) {
                    Message message = listenSession.consumer().receive();
                    if (message == null) {
                        LOG.log(Level.WARNING,
                                "Null message received from message consumer.",
                                " Exiting ListenerThread::run().");
                        return;
                    }
                    while (message != null) {
                        Executor executor = theTransport.callback.getExecutor();
                        if (executor == null) {
                            executor = theTransport.theBus
                                .getWorkQueueManager().getAutomaticWorkQueue();
                        }
                        if (executor != null) {
                            try {
                                executor.execute(new JMSExecutor(theTransport, message));
                                message = null;
                            } catch (RejectedExecutionException ree) {
                                //FIXME - no room left on workqueue, what to do
                                //for now, loop until it WILL fit on the queue, 
                                //although we could just dispatch on this thread.
                            }                            
                        } else {
                            //shouldn't ever get here....
                            try {
                                theTransport.incoming(message);
                            } catch (IOException ex) {
                                LOG.log(Level.WARNING, "Failed to process incoming message : ", ex);
                            }
                            message = null;
                        }
                    }
                }
            } catch (JMSException jmsex) {
                jmsex.printStackTrace();
                LOG.log(Level.SEVERE, "Exiting ListenerThread::run(): ", jmsex.getMessage());
            } catch (Throwable jmsex) {
                jmsex.printStackTrace();
                LOG.log(Level.SEVERE, "Exiting ListenerThread::run(): ", jmsex.getMessage());
            }
        }
    }
    
    static class JMSExecutor implements Runnable {
        Message message;
        JMSServerTransport transport;
        
        JMSExecutor(JMSServerTransport t, Message m) {
            message = m;
            transport = t;
        }

        public void run() {
            try {
                transport.incoming(message);
            } catch (IOException ex) {
                //TODO: Decide what to do if we receive the exception.
                LOG.log(Level.WARNING,
                        "Failed to process incoming message : ", ex);
            }
        }
        
    }
}
