package org.objectweb.celtix.transports.http;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.objectweb.celtix.Bus;

import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

public class JettyHTTPDestination extends AbstractHTTPDestination {
    
    public static final String HTTP_REQUEST =
        JettyHTTPDestination.class.getName() + ".REQUEST";
    public static final String HTTP_RESPONSE =
        JettyHTTPDestination.class.getName() + ".RESPONSE";
    
    protected ServerEngine engine;
    protected MessageObserver incomingObserver;

    /**
     * Constructor, using real configuration and Jetty server engine.
     * 
     * @param b the associated Bus
     * @param ref the published endpoint
     * @throws WSDLException
     * @throws IOException
     */
    public JettyHTTPDestination(Bus b, EndpointReferenceType ref)
        throws WSDLException, IOException {
        this(b, ref, null, new HTTPDestinationConfiguration(b, ref));
    }

    /**
     * Constructor, allowing subsititution of configuration.
     * 
     * @param b the associated Bus
     * @param ref the published endpoint
     * @param eng the server engine
     * @param cfg the configuration
     * @throws WSDLException
     * @throws IOException
     */

    public JettyHTTPDestination(Bus b, 
                                EndpointReferenceType ref,
                                ServerEngine eng,
                                HTTPDestinationConfiguration cfg)
        throws WSDLException, IOException {
        super(b, ref, cfg);
        engine = eng != null 
                 ? eng
                 : JettyHTTPServerEngine.getForPort(bus, nurl.getProtocol(), nurl.getPort());
    }

    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    public synchronized void setMessageObserver(MessageObserver observer) {
        if (null != observer) {
            LOG.info("registering incoming observer: " + observer);

            if (config.contextMatchOnStem()) {
                engine.addServant(config.getAddress(), new AbstractHttpHandler() {
                        public void handle(String pathInContext, String pathParams,
                                           HttpRequest req, HttpResponse resp)
                            throws IOException {
                            if (pathInContext.startsWith(getName())) {
                                doService(req, resp);                    
                            }
                        }
                    });
            } else {
                engine.addServant(config.getAddress(), new AbstractHttpHandler() {
                        public void handle(String pathInContext, String pathParams,
                                           HttpRequest req, HttpResponse resp)
                            throws IOException {
                            if (pathInContext.equals(getName())) {
                                doService(req, resp);
                            }
                        }
                    });
            }
        } else {
            LOG.info("unregistering incoming observer: " + incomingObserver);
            engine.removeServant(nurl);            
        }
        incomingObserver = observer;
    }
    
    /**
     * Retreive a back-channel Conduit, which must be policy-compatible
     * with the current Message and associated Destination. For example
     * compatible Quality of Protection must be asserted on the back-channel.
     * This would generally only be an issue if the back-channel is decoupled.
     * 
     * @param message the current message (null to indicate a disassociated
     * back-channel.
     * @param address the backchannel address (null to indicate anonymous)
     * @return a suitable Conduit
     */
    public Conduit getBackChannel(Message message,
                                  EndpointReferenceType address) {
        HttpResponse response = (HttpResponse)message.get(HTTP_RESPONSE);
        return new BackChannelConduit(address, response);
    }

    /**
     * Shutdown the Destination, i.e. stop accepting incoming messages.
     */
    public void shutdown() {  
    }
        
    /**
     * Copy the request headers into the message.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected void copyRequestHeaders(Message message,
                                      Map<String, List<String>> headers) {
        HttpRequest req = (HttpRequest)message.get(HTTP_REQUEST);
        for (Enumeration e = req.getFieldNames(); e.hasMoreElements();) {
            String fname = (String)e.nextElement();
            List<String> values;
            if (headers.containsKey(fname)) {
                values = headers.get(fname);
            } else {
                values = new ArrayList<String>();
                headers.put(fname, values);
            }
            for (Enumeration e2 = req.getFieldValues(fname); e2.hasMoreElements();) {
                String val = (String)e2.nextElement();
                values.add(val);
            }
        }        
    }
    
    /**
     * Copy the response headers into the response.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected void copyResponseHeaders(Message message, HttpResponse response) {
        Map<?, ?> headers = (Map<?, ?>)message.get(MessageContext.HTTP_RESPONSE_HEADERS);
        if (null != headers) {
            for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                String header = (String)iter.next();
                List<?> headerList = (List<?>)headers.get(header);
                for (Object value : headerList) {
                    response.addField(header, (String)value);
                }
            }
        }
    }
    
    protected void doService(HttpRequest req, HttpResponse resp)
        throws IOException {
        if (config.getPolicy().isSetRedirectURL()) {
            resp.sendRedirect(config.getPolicy().getRedirectURL());
            resp.commit();
            req.setHandled(true);
            return;
        }    
        
        if ("GET".equals(req.getMethod()) && req.getURI().toString().toLowerCase().endsWith("?wsdl")) {
            try {
                
                Definition def = EndpointReferenceUtils.getWSDLDefinition(
                    bus.getExtension(WSDLManager.class), reference);
                resp.addField("Content-Type", "text/xml");
                OutputStream os = resp.getOutputStream();
                bus.getExtension(WSDLManager.class).getWSDLFactory().newWSDLWriter().writeWSDL(def, os);
                resp.getOutputStream().flush();
                resp.commit();
                req.setHandled(true);
                return;
            } catch (WSDLException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
        
        // REVISIT: service on executor if associated with endpoint
        serviceRequest(req, resp);
    }
    
    protected void serviceRequest(final HttpRequest req, final HttpResponse resp)
        throws IOException {
        try {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Service http request on thread: " + Thread.currentThread());
            }
            
            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, req.getInputStream());
            inMessage.put(HTTP_REQUEST, req);
            inMessage.put(HTTP_RESPONSE, resp);
            inMessage.put(MessageContext.HTTP_REQUEST_METHOD, req.getMethod());
            inMessage.put(MessageContext.PATH_INFO, req.getPath());
            inMessage.put(MessageContext.QUERY_STRING, req.getQuery());

            setHeaders(inMessage);
            
            Exchange exchange = new ExchangeImpl();
            exchange.setDestination(this);
            exchange.setInMessage(inMessage);
            inMessage.setExchange(exchange);
            
            incomingObserver.onMessage(inMessage);
            
            resp.commit();
            req.setHandled(true);
        } finally {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
    }

    protected OutputStream flushHeaders(Message outMessage) throws IOException {
        Object responseObj = outMessage.get(HTTP_RESPONSE);
        OutputStream responseStream = null;
        if (responseObj instanceof HttpResponse) {
            // non-decoupled response
            HttpResponse response = (HttpResponse)responseObj;
                
            Integer i = (Integer)outMessage.get(HTTP_RESPONSE_CODE);
            if (i != null) {
                if (i.intValue() == 500) {
                    response.setStatus(i.intValue(), "Fault Occurred");
                } else {
                    response.setStatus(i.intValue());
                }
            } else {
                response.setStatus(200);
            }
            
            copyResponseHeaders(outMessage, response);
            responseStream = response.getOutputStream();
                    
            if (isOneWay(outMessage)) {
                response.commit();
            }
        } else {
            LOG.log(Level.WARNING, "UNEXPECTED_RESPONSE_TYPE_MSG", responseObj.getClass());
            throw new IOException("UNEXPECTED_RESPONSE_TYPE_MSG" + responseObj.getClass());
        }
    
        if (isOneWay(outMessage)) {
            outMessage.remove(HTTP_RESPONSE);
        }
        return responseStream;
    }
    
    protected boolean isOneWay(Message message) {
        // REVISIT need a mechanism whereby higher level (binding, WS-A layer
        // or frontend) marks message as oneway
        return false;
    }
    
    /**
     * Backchannel conduit, flushes headers on send.
     */
    private class BackChannelConduit implements Conduit {
        
        protected HttpResponse response;
        protected EndpointReferenceType target;
        
        BackChannelConduit(EndpointReferenceType ref, HttpResponse resp) {
            response = resp;
            target = ref;
        }
        
        /**
         * Register a message observer for incoming messages.
         * 
         * @param observer the observer to notify on receipt of incoming
         */
        public void setMessageObserver(MessageObserver observer) {
            // shouldn't be called for a back channel conduit
        }

        /**
         * Send an outbound message, assumed to contain all the name-value
         * mappings of the corresponding input message (if any). 
         * 
         * @param message the message to be sent.
         */
        public void send(Message message) throws IOException {
            message.put(HTTP_RESPONSE, response);
            message.setContent(OutputStream.class,
                               new WrappedOutputStream(message, response));
        }
        
        /**
         * @return the reference associated with the target Destination
         */    
        public EndpointReferenceType getTarget() {
            return target;
        }
        
        /**
         * Retreive the back-channel Destination.
         * 
         * @return the backchannel Destination (or null if the backchannel is
         * built-in)
         */
        public Destination getBackChannel() {
            return null;
        }
        
        /**
         * Close the conduit
         */
        public void close() {
        }
    }
    
    /**
     * Wrapper stream responsible for flushing headers and committing outgoing
     * HTTP-level response.
     */
    private class WrappedOutputStream extends AbstractWrappedOutputStream {
        
        protected HttpResponse response;
        
        WrappedOutputStream(Message m, HttpResponse resp) {
            super(m);
            response = resp;
        }

        /**
         * Perform any actions required on stream flush (freeze headers,
         * reset output stream ... etc.)
         */
        protected void doFlush() throws IOException {
            OutputStream responseStream = flushHeaders(outMessage);
            if (null != responseStream) {
                resetOut(responseStream);
            }
        }

        /**
         * Perform any actions required on stream closure (handle response etc.)
         */
        protected void doClose() {
            commitResponse();
        }

        private void commitResponse() {
            try {
                response.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
