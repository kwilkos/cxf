package org.objectweb.celtix.transports.http;

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
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.helpers.CastUtils;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


/**
 * HTTP Conduit implementation.
 */
public class HTTPConduit implements Conduit {

    static final String HTTP_CONNECTION = "http.connection";
    private static final Logger LOG = LogUtils.getL7dLogger(HTTPConduit.class);

    private final HTTPConduitConfiguration config;
    private final URLConnectionFactory connectionFactory;
    private URL url;
    private MessageObserver incomingObserver;
    private EndpointReferenceType target;

    /**
     * Constructor, using real configuration.
     * 
     * @param bus the bus
     * @param endpointInfo the endpoint info of the initiator
     * @throws IOException
     */
    public HTTPConduit(Bus bus, EndpointInfo endpointInfo) throws IOException {
        this(bus,
             endpointInfo,
             null);
    }

    /**
     * Constructor, using real configuration.
     * 
     * @param bus the bus
     * @param endpointInfo the endpoint info of the initiator
     * @param target the endpoint reference of the target
     * @throws IOException
     */
    public HTTPConduit(Bus bus, EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        this(endpointInfo,
             target,
             null,
             new HTTPConduitConfiguration(bus, endpointInfo));
    }

    /**
     * Constructor, allowing subsititution of configuration.
     * 
     * @param endpointInfo the endpoint info of the initiator
     * @param target the endpoint reference of the target
     * @param factory the URL connection factory
     * @param cfg the configuration
     * @throws IOException
     */
    public HTTPConduit(EndpointInfo endpointInfo,
                       EndpointReferenceType t,
                       URLConnectionFactory factory,
                       HTTPConduitConfiguration cfg) throws IOException {
        config = cfg;
        connectionFactory = factory != null
                            ? factory
                            : getDefaultConnectionFactory();
        url = new URL(config.getAddress());       

        if (null == t) {
            target = new EndpointReferenceType();
            AttributedURIType address = new AttributedURIType();
            address.setValue(config.getAddress());
            target.setAddress(address);
        } else {
            target = t;
        }
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
        // REVISIT if decoupled, return Destination for response endpoint
        return null;
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
    
        // REVISIT if decoupled, close response Destination if reference
        // count hits zero
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
    private void flushHeaders(Message message) throws IOException {
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
     * Wrapper stream responsible for flushing headers and handling incoming
     * HTTP-level response (not necessarily the MEP response).
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
            flushHeaders(outMessage);
            resetOut(connection.getOutputStream());
        }

        /**
         * Perform any actions required on stream closure (handle response etc.)
         */
        protected void doClose() throws IOException {
            handleResponse();
        }

        private void handleResponse() throws IOException {
            // REVISIT distinguish decoupled case
            Message inMessage = new MessageImpl();
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

}