package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.common.util.Base64Exception;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public abstract class AbstractHTTPServerTransport implements ServerTransport {
    static final Logger LOG = LogUtils.getL7dLogger(AbstractHTTPServerTransport.class);
    
    private static final long serialVersionUID = 1L;

    protected EndpointReferenceType reference;
    protected String url;
    protected String name;
    protected URL nurl;
    protected ServerTransportCallback callback;
    protected Configuration configuration;
    protected HTTPServerPolicy policy;
    protected final Bus bus;
    
    public AbstractHTTPServerTransport(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        if (b == null) {
            Thread.dumpStack();
        }
        reference = ref;
        bus = b;
        // get url (publish address) from endpoint reference
        url = EndpointReferenceUtils.getAddress(ref);  
        configuration = createConfiguration(ref);
        
        nurl = new URL(url);
        name = nurl.getPath();
        policy = getServerPolicy(configuration);
    }
    
    private HTTPServerPolicy getServerPolicy(Configuration conf) {
        HTTPServerPolicy pol = conf.getObject(HTTPServerPolicy.class, "httpServer");
        if (pol == null) {
            pol = new HTTPServerPolicy();
        }
        return pol;
    }
    
    public void rebase(MessageContext context, EndpointReferenceType decoupledResponseEndpoint)
        throws IOException {
    }

    public void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context) {
        // Do not need to do anything here. 
    }
    
    public void shutdown() {
    }
    
    private Configuration createConfiguration(EndpointReferenceType ref) {
        Configuration busConfiguration = bus.getConfiguration();
        QName serviceName = EndpointReferenceUtils.getServiceName(ref);
        Configuration endpointConfiguration = busConfiguration
            .getChild("http://celtix.objectweb.org/bus/jaxws/endpoint-config", serviceName);
        Port port = null;
        try {
            port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);            
        } catch (WSDLException ex) {
            // ignore
        }
        return new HTTPServerTransportConfiguration(endpointConfiguration, port);
    }

    
    protected void setHeaders(MessageContext ctx) {
        ctx.put(ObjectMessageContext.MESSAGE_INPUT, true);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        copyRequestHeaders(ctx, headers);
        ctx.put(GenericMessageContext.HTTP_REQUEST_HEADERS, headers);

        
        if (headers.containsKey("Authorization")) {
            List<String> authorizationLines = headers.get("Authorization"); 
            String credentials = authorizationLines.get(0);
            String authType = credentials.split(" ")[0];
            if ("Basic".equals(authType)) {
                String authEncoded = credentials.split(" ")[1];
                try {
                    String authDecoded = new String(Base64Utility.decode(authEncoded));
                    String authInfo[] = authDecoded.split(":");
                    String username = authInfo[0];
                    String password = authInfo[1];
                    ctx.put(BindingProvider.USERNAME_PROPERTY, username);
                    ctx.put(BindingProvider.PASSWORD_PROPERTY, password);
                } catch (Base64Exception ex) {
                    //ignore, we'll leave things alone.  They can try decoding it themselves
                }
            }
        }
        
        headers = new HashMap<String, List<String>>();
        setPolicies(ctx, headers);
        ctx.put(GenericMessageContext.HTTP_RESPONSE_HEADERS, headers);         
    }
    protected void setPolicies(MessageContext ctx, Map<String, List<String>> headers) {
        if (policy.isSetCacheControl()) {
            headers.put("Cache-Control",
                        Arrays.asList(new String[] {policy.getCacheControl().value()}));
        }
        if (policy.isSetContentLocation()) {
            headers.put("Content-Location",
                        Arrays.asList(new String[] {policy.getContentLocation()}));
        }
        if (policy.isSetContentEncoding()) {
            headers.put("Content-Encoding",
                        Arrays.asList(new String[] {policy.getContentEncoding()}));
        }
        if (policy.isSetContentType()) {
            headers.put("Content-Type",
                        Arrays.asList(new String[] {policy.getContentType()}));
        }
        if (policy.isSetServerType()) {
            headers.put("Server",
                        Arrays.asList(new String[] {policy.getServerType()}));
        }
        if (policy.isSetHonorKeepAlive() && !policy.isHonorKeepAlive()) {
            headers.put("Connection",
                        Arrays.asList(new String[] {"close"}));
        }
        
    /*
     * TODO - hook up these policies
    <xs:attribute name="SuppressClientSendErrors" type="xs:boolean" use="optional" default="false">
    <xs:attribute name="SuppressClientReceiveErrors" type="xs:boolean" use="optional" default="false">
    */

    }
    
    /**
     * @param context The <code>OutputStreamMessageContext</code> to prepare.
     */
    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context)
        throws IOException {
        ((AbstractHTTPServerOutputStreamContext)context).flushHeaders();
    }    

    protected abstract void copyRequestHeaders(MessageContext ctx, Map<String, List<String>> headers);
    

}
