package org.objectweb.celtix.transports.http;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.ws.BindingProvider;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.common.util.Base64Exception;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


/**
 * Common base for HTTP Destination implementations.
 */
public abstract class AbstractHTTPDestination  implements Destination {
    static final Logger LOG = LogUtils.getL7dLogger(AbstractHTTPDestination.class);
    
    private static final long serialVersionUID = 1L;        

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected final HTTPDestinationConfiguration config;
    protected String name;
    protected URL nurl;

    /**
     * Constructor, using real configuration.
     * 
     * @param b the associated Bus
     * @param ref the published endpoint
     * @throws WSDLException
     * @throws IOException
     */
    public AbstractHTTPDestination(Bus b, EndpointReferenceType ref)
        throws WSDLException, IOException {
        this(b,
             ref,
             new HTTPDestinationConfiguration(b, ref));
    }

    /**
     * Constructor, allowing subsititution of configuration.
     * 
     * @param b the associated Bus
     * @param ref the published endpoint
     * @param cfg the configuration
     * @throws WSDLException
     * @throws IOException
     */    
    public AbstractHTTPDestination(Bus b,
                                   EndpointReferenceType ref,
                                   HTTPDestinationConfiguration cfg)
        throws WSDLException, IOException {
        bus = b;
        reference = ref;
        config = cfg;
        
        nurl = new URL(config.getAddress());
        name = nurl.getPath();
    }

    /**
     * @return the reference associated with this Destination
     */    
    public EndpointReferenceType getAddress() {
        return reference;
    }

    /**
     * Cache HTTP headers in message.
     * 
     * @param message the current message
     */
    protected void setHeaders(Message message) {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        copyRequestHeaders(message, requestHeaders);
        message.put(HTTP_REQUEST_HEADERS, requestHeaders);

        if (requestHeaders.containsKey("Authorization")) {
            List<String> authorizationLines = requestHeaders.get("Authorization"); 
            String credentials = authorizationLines.get(0);
            String authType = credentials.split(" ")[0];
            if ("Basic".equals(authType)) {
                String authEncoded = credentials.split(" ")[1];
                try {
                    String authDecoded = new String(Base64Utility.decode(authEncoded));
                    String authInfo[] = authDecoded.split(":");
                    String username = authInfo[0];
                    String password = authInfo[1];
                    message.put(BindingProvider.USERNAME_PROPERTY, username);
                    message.put(BindingProvider.PASSWORD_PROPERTY, password);
                } catch (Base64Exception ex) {
                    //ignore, we'll leave things alone.  They can try decoding it themselves
                }
            }
        }
        
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        config.setPolicies(responseHeaders);
        message.put(HTTP_RESPONSE_HEADERS, responseHeaders);         
    }
    
    /**
     * Copy the request headers into the message.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected abstract void copyRequestHeaders(Message message,
                                               Map<String, List<String>> headers);
}
