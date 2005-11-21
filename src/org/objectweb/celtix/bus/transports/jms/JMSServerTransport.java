package org.objectweb.celtix.bus.transports.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.naming.NamingException;
import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.jms.context.JMSServerHeadersType;


public class JMSServerTransport extends JMSTransportBase implements ServerTransport {
    private static final Logger LOG = LogUtils.getL7dLogger(JMSServerTransport.class);
    private static final String JMS_SERVER_TRANSPORT_MESSAGE =
        JMSServerTransport.class.getName() + ".IncomingMessage";
    private static final String JMS_SERVER_TRANSPORT_CORRELATION_ID =
        JMSServerTransport.class.getName() + ".CorrelationID";

    private PooledSession listenerSession;
    private ServerTransportCallback callback;
    private Thread listenerThread;

    
    public JMSServerTransport(Bus bus, EndpointReferenceType address) 
        throws WSDLException {
        super(bus, address);
        entry("JMSServerTransport Constructor");
    }

    public void activate(ServerTransportCallback transportCB) throws IOException {
        entry("JMSServerTransport activate() with WorkQueue");
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
    


//        callback.transportActivated();
    }

    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        OutputStreamMessageContext osmc = new JMSOutputStreamContext(context);
        
        if (context instanceof JMSInputStreamContext) {
            ((JMSInputStreamContext) context).setMatchingOutCtx(osmc);
        }
        return osmc;
        
    }
    
    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
    }
    
    
    public void deactivate() throws IOException {
        //throw new IOException("deactivate() is not implemented for JMSServerTransport.");
    }

    /**
     * Called by the native ServerTransport::shutdown(), giving
     * notice that the corresponding thread blocked on run()
     * should be interrupted, and in the case of the last such thread,
     * that the connection maintained the underlying middleware is no
     * longer required by this transport instance.
     * <p>
     * Note that the mechanism used to shrink the set of connect()
     * thread during idle periods is broken, as there's no way in
     * disconnect() to determine which thread blocked in connect() to
     * interrupt.  These connect() threads are not interchangable as
     * they have thread local storage associated by the native
     * code. We work-around this restriction by restricting disconnect()
     * to only being called on shutdown, by configure the high and low
     * water marks to the same value.
     */
    public void shutdown() {
        entry("JMSServerTransport shutdown()");
        sessionFactory.shutdown();
    }

    public void postDispatch(JMSOutputStreamContext ctx, 
                                           Message message, 
                                           String correlationID) 
        throws JMSException {
        // ensure non-oneways in point-to-point domain

        if (!ctx.isOneWay()) {
            if (queueDestinationStyle) {
                // send reply
                Queue replyTo = (Queue)message.getJMSReplyTo();
                PooledSession replySession = sessionFactory.get(false);

                try {
                    Message reply;
                    if (textPayload) {
                        reply = marshal(ctx.getOutputStream().toString(), replySession.session(), null);
                    } else {
                        reply = marshal(((ByteArrayOutputStream) ctx.getOutputStream()).toByteArray(), 
                                               replySession.session(), 
                                               null);
                    }

                    if (correlationID != null && !"".equals(correlationID)) {
                        reply.setJMSCorrelationID(correlationID);
                    }
    
                    QueueSender sender = (QueueSender)replySession.producer();
  
                    JMSServerHeadersType headers =
                        (JMSServerHeadersType) ctx.get(JMSConstants.JMS_RESPONSE_HEADERS);
    
                    int deliveryMode = getJMSDeliveryMode(headers);
                    int priority = getJMSPriority(headers);
                    long ttl = getTimeToLive(headers);
    
                    setMessageProperties(headers, message);
        
                    LOG.log(Level.FINE, "server sending reply: ", reply);
                    
                    sender.send(replyTo, reply, deliveryMode, priority, ttl);
                } finally {
                    // house-keeping
                    sessionFactory.recycle(replySession);
                }
            } else {
                // we will never receive a non-oneway invocation in pub-sub
                // domain from Artix client - however a mis-behaving pure JMS
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
                && jmsAddressDetails.isUseMessageIDAsCorrelationID()) {
                correlationID = message.getJMSMessageID();
            }

            Object request = unmarshal(message);

            byte[] bytes = null;
            
            if (textPayload) {
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
            
            // TODO: Create the output Stream context in createOutputStreamContext() 
            // and InputStreamContext should hold on to the reference of this 
            // OutputStream context and this way we can get 
            //the outputcontext after dispatch.
            this.postDispatch((JMSOutputStreamContext) context.getMatchingOutCtx(), 
                                         message, 
                                         correlationID);
            
        } catch (JMSException jmsex) {
            //TODO: need to revisit for which exception should we throw.
            throw new IOException(jmsex.getMessage());
        }
    }
    
    class JMSListenerThread extends Thread {
        private final PooledSession listenerSession;
        private final JMSServerTransport theTransport;
        private Message message;

        public JMSListenerThread(PooledSession session,
                                 JMSServerTransport transport) {
            listenerSession = session;
            theTransport = transport;
        }

        public void run() {
            try {
                while (true) {
                    message = listenerSession.consumer().receive();
                    if (message != null) {
//                        WorkItem item = new JMSWorkItem(message, theTransport);
//                        theQueue.enqueue(item, -1);
                        theTransport.callback.getExecutor().execute(new Runnable() {
                            public void run() {
                                try {
                                    theTransport.incoming(message);
                                } catch (IOException ex) {
                                    //TODO: Decide what to do if we receive the exception.
                                    LOG.log(Level.WARNING, "Failed to process incoming message : ", ex);
                                }                                
                            }
                        });
                    } else {
                        LOG.log(Level.WARNING,
                                                      "Null message received from message consumer.",
                                                      " Exiting ListenerThread::run().");
                        break;
                    }
                }
            } catch (JMSException jmsex) {
                LOG.log(Level.SEVERE, "Exiting ListenerThread::run(): ", jmsex.getMessage());
            }
        }
    }
    
//    static class JMSServerInputStreamContext 
//        extends JMSInputStreamContext {
//        
//        public JMSServerInputStreamContext(InputStream ins) {
//            super(ins);
//        }
//    }
}