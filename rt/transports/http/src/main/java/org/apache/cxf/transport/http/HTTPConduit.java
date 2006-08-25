package org.apache.cxf.transport.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.ws.BindingProvider;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

/**
 * HTTP Conduit implementation.
 */
public class HTTPConduit implements Conduit {

    public static final String HTTP_CONNECTION = "http.connection";
    
    private static final Logger LOG = LogUtils.getL7dLogger(HTTPConduit.class);
    private final Bus bus;
    private final HTTPConduitConfiguration config;
    private final URLConnectionFactory connectionFactory;
    private URL url;
    private MessageObserver incomingObserver;
    private EndpointReferenceType target;

    private ServerEngine decoupledEngine;
    private URL decoupledURL;
    private DecoupledDestination decoupledDestination;

    /**
     * Constructor, using real configuration.
     * 
     * @param b the associated Bus
     * @param endpointInfo the endpoint info of the initiator
     * @throws IOException
     */
    public HTTPConduit(Bus b, EndpointInfo endpointInfo) throws IOException {
        this(b,
             endpointInfo,
             null);
    }

    /**
     * Constructor, using real configuration.
     * 
     * @param b the associated Bus
     * @param endpointInfo the endpoint info of the initiator
     * @param target the endpoint reference of the target
     * @throws IOException
     */
    public HTTPConduit(Bus b,
                       EndpointInfo endpointInfo,
                       EndpointReferenceType target) throws IOException {
        this(b,
             endpointInfo,
             target,
             null,
             null,
             new HTTPConduitConfiguration(b, endpointInfo));
    }

    /**
     * Constructor, allowing subsititution of configuration, 
     * connnection factory ang decoupled engine.
     * 
     * @param b the associated Bus
     * @param endpointInfo the endpoint info of the initiator
     * @param target the endpoint reference of the target
     * @param factory the URL connection factory
     * @param eng the decoupled engine
     * @param cfg the configuration
     * @throws IOException
     */
    public HTTPConduit(Bus b,
                       EndpointInfo endpointInfo,
                       EndpointReferenceType t,
                       URLConnectionFactory factory,
                       ServerEngine eng,
                       HTTPConduitConfiguration cfg) throws IOException {
        bus = b;
        config = cfg;
        connectionFactory = factory != null
                            ? factory
                            : getDefaultConnectionFactory();
        decoupledEngine = eng;
        url = new URL(config.getAddress());
        target = getTargetReference(t);
    }
    
    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    public void setMessageObserver(MessageObserver observer) {
        incomingObserver = observer;
        LOG.info("registering incoming observer: " + incomingObserver);
    }

    /**
     * Send an outbound message.
     * 
     * @param message the message to be sent.
     */
    public void send(Message message) throws IOException {
        Map<String, List<String>> headers = setHeaders(message);
        
        String value = (String)message.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        URL currentURL = value != null ? new URL(value) : url;

        URLConnection connection = 
            connectionFactory.createConnection(config.getProxy(), currentURL);
        connection.setDoOutput(true);

        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            hc.setRequestMethod("POST");
        }

        connection.setConnectTimeout((int)config.getPolicy().getConnectionTimeout());
        connection.setReadTimeout((int)config.getPolicy().getReceiveTimeout());

        connection.setUseCaches(false);
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            if (config.getPolicy().isAutoRedirect()) {
                //cannot use chunking if autoredirect as the request will need to be
                //completely cached locally and resent to the redirect target
                hc.setInstanceFollowRedirects(true);
            } else {
                hc.setInstanceFollowRedirects(false);
                if (config.getPolicy().isAllowChunking()) {
                    hc.setChunkedStreamingMode(2048);
                }
            }
        }

        config.setPolicies(message, headers);
     
        message.setContent(OutputStream.class,
                           new WrappedOutputStream(message, connection));
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
    public synchronized Destination getBackChannel() {
        if (decoupledDestination == null
            && config.getPolicy().getDecoupledEndpoint() != null) {
            decoupledDestination = setUpDecoupledDestination(); 
        }
        return decoupledDestination;
    }
    
    public void close(Message msg) throws IOException {
        msg.getContent(OutputStream.class).close();        
    }

    /**
     * Close the conduit
     */
    public void close() {
        if (url != null) {
            try {
                URLConnection connect = url.openConnection();
                if (connect instanceof HttpURLConnection) {
                    ((HttpURLConnection)connect).disconnect();
                }
            } catch (IOException ex) {
                //ignore
            }
            url = null;
        }
    
        // in decoupled case, close response Destination if reference count
        // hits zero
        //
        if (decoupledURL != null && decoupledEngine != null) {
            DecoupledHandler decoupledHandler = 
                (DecoupledHandler)decoupledEngine.getServant(decoupledURL);
            if (decoupledHandler != null) {
                decoupledHandler.release();
            }
        }
    }

    /**
     * @return the encapsulated URL
     */
    protected URL getURL() {
        return url;
    }
    
    /**
     * @return default URLConnectionFactory
     */
    private URLConnectionFactory getDefaultConnectionFactory() {
        return new URLConnectionFactory() {
            public URLConnection createConnection(Proxy proxy, URL u)
                throws IOException {
                return config.getProxy() != null 
                        ? u.openConnection(proxy)
                        : u.openConnection();
            }
        };
    }
    
    /**
     * Get the target reference which may be constructor-provided or 
     * configured.
     * 
     * @param t the constructor-provider target
     * @return the actual target
     */
    private EndpointReferenceType getTargetReference(EndpointReferenceType t) {
        EndpointReferenceType ref = null;
        if (null == t) {
            ref = new EndpointReferenceType();
            AttributedURIType address = new AttributedURIType();
            address.setValue(config.getAddress());
            ref.setAddress(address);
        } else {
            ref = t;
        }
        return ref;
    }

    /**
     * Ensure an initial set of header is availbale on the outbound message.
     * 
     * @param message the outbound message
     * @return the headers
     */
    private Map<String, List<String>> setHeaders(Message message) {
        Map<String, List<String>> headers =
            CastUtils.cast((Map<?, ?>)message.get(HTTP_REQUEST_HEADERS));
        if (null == headers) {
            headers = new HashMap<String, List<String>>();
            message.put(HTTP_REQUEST_HEADERS, headers);
        }
        return headers;
    }
    
    /**
     * Flush the headers onto the output stream.
     * 
     * @param message the outbound message
     * @throws IOException
     */
    protected void flushHeaders(Message message) throws IOException {
        Map<String, List<String>> headers = 
            CastUtils.cast((Map<?, ?>)message.get(HTTP_REQUEST_HEADERS));
        URLConnection connection = (URLConnection)message.get(HTTP_CONNECTION);
        if (null != headers) {
            for (String header : headers.keySet()) {
                List<String> headerList = headers.get(header);
                for (String value : headerList) {
                    connection.addRequestProperty(header, value);
                }
            }
        }
    }
   
    /**
     * Retrieve the respons code.
     * 
     * @param connection the URLConnection
     * @return the response code
     * @throws IOException
     */
    private int getResponseCode(URLConnection connection) throws IOException {
        int responseCode = HttpURLConnection.HTTP_OK;
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            responseCode = hc.getResponseCode();
        } else {
            if (connection.getHeaderField(HTTP_RESPONSE_CODE) != null) {
                responseCode =
                    Integer.parseInt(connection.getHeaderField(HTTP_RESPONSE_CODE));
            }
        }
        return responseCode;
    }

    /**
     * Set up the decoupled Destination if necessary.
     * 
     * @return an appropriate decoupled Destination
     */
    private DecoupledDestination setUpDecoupledDestination() {        
        EndpointReferenceType reference =
            EndpointReferenceUtils.getEndpointReference(config.getPolicy().getDecoupledEndpoint());
        if (reference != null) {
            String decoupledAddress = reference.getAddress().getValue();
            LOG.info("creating decoupled endpoint: " + decoupledAddress);
            try {
                decoupledURL = new URL(decoupledAddress);
                if (decoupledEngine == null) {
                    decoupledEngine = 
                        JettyHTTPServerEngine.getForPort(bus, 
                                                         decoupledURL.getProtocol(),
                                                         decoupledURL.getPort());
                }
                DecoupledHandler decoupledHandler =
                    (DecoupledHandler)decoupledEngine.getServant(decoupledURL);
                if (decoupledHandler == null) {
                    decoupledHandler = new DecoupledHandler();
                    decoupledEngine.addServant(decoupledURL, decoupledHandler);
                } 
                decoupledHandler.duplicate();
            } catch (Exception e) {
                // REVISIT move message to localizable Messages.properties
                LOG.log(Level.WARNING, "decoupled endpoint creation failed: ", e);
            }
        }
        return new DecoupledDestination(reference, incomingObserver);
    }

    /**
     * Wrapper output stream responsible for flushing headers and handling
     * the incoming HTTP-level response (not necessarily the MEP response).
     */
    private class WrappedOutputStream extends AbstractWrappedOutputStream {
        protected URLConnection connection;
        
        WrappedOutputStream(Message m, URLConnection c) {
            super(m);
            connection = c;
        }

        /**
         * Perform any actions required on stream flush (freeze headers,
         * reset output stream ... etc.)
         */
        protected void doFlush() throws IOException {
            if (!alreadyFlushed()) {
                flushHeaders(outMessage);
                resetOut(connection.getOutputStream(), true);
            }
        }

        /**
         * Perform any actions required on stream closure (handle response etc.)
         */
        protected void doClose() throws IOException {
            handleResponse();
        }
        
        protected void onWrite() throws IOException {
            
        }

        private void handleResponse() throws IOException {
            // REVISIT distinguish decoupled case
            Message inMessage = new MessageImpl();
            inMessage.setExchange(outMessage.getExchange());
            InputStream in = null;
            inMessage.put(HTTP_RESPONSE_HEADERS, connection.getHeaderFields());
            inMessage.put(HTTP_RESPONSE_CODE, getResponseCode(connection));
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                in = hc.getErrorStream();
                if (null == in) {
                    in = connection.getInputStream();
                }
            } else {
                in = connection.getInputStream();
            }
            
            inMessage.setContent(InputStream.class, in);
            
            incomingObserver.onMessage(inMessage);
        }
    }

    /**
     * Wrapper output stream responsible for commiting incoming request 
     * containing a decoupled response.
     */
    private class WrapperInputStream extends FilterInputStream {
        HttpRequest request;
        HttpResponse response;
        
        WrapperInputStream(HttpRequest req,
                           HttpResponse resp) {
            super(req.getInputStream());
            request = req;
            response = resp;
        }
        
        public void close() throws IOException {
            super.close();            
            response.commit();
            request.setHandled(true);
        }
    }
    
    /**
     * Represented decoupled response endpoint.
     */
    protected class DecoupledDestination implements Destination {
        protected MessageObserver decoupledMessageObserver;
        private EndpointReferenceType address;
        
        DecoupledDestination(EndpointReferenceType ref,
                             MessageObserver incomingObserver) {
            address = ref;
            decoupledMessageObserver = incomingObserver;
        }

        public EndpointReferenceType getAddress() {
            return address;
        }

        public Conduit getBackChannel(Message inMessage,
                                      Message partialResponse,
                                      EndpointReferenceType addr)
            throws WSDLException, IOException {
            // shouldn't be called on decoupled endpoint
            return null;
        }

        public void shutdown() {
            // TODO Auto-generated method stub            
        }

        public synchronized void setMessageObserver(MessageObserver observer) {
            decoupledMessageObserver = observer;
        }
        
        protected synchronized MessageObserver getMessageObserver() {
            return decoupledMessageObserver;
        }
    }

    /**
     * Handles incoming decoupled responses.
     */
    private class DecoupledHandler extends AbstractHttpHandler {
        private int refCount;
                
        synchronized void duplicate() {
            refCount++;
        }
        
        synchronized void release() {
            if (--refCount == 0) {
                decoupledEngine.removeServant(decoupledURL);
                JettyHTTPServerEngine.destroyForPort(decoupledURL.getPort());
            }
        }
        
        public void handle(String pathInContext, 
                           String pathParams,
                           HttpRequest req,
                           HttpResponse resp) throws IOException {
            Message inMessage = new MessageImpl();
            inMessage.put(HTTP_RESPONSE_HEADERS, req.getParameters());
            inMessage.put(HTTP_RESPONSE_CODE, HttpURLConnection.HTTP_OK);
            InputStream is = new WrapperInputStream(req, resp);
            inMessage.setContent(InputStream.class, is);

            decoupledDestination.getMessageObserver().onMessage(inMessage);
        }
    }    
}
