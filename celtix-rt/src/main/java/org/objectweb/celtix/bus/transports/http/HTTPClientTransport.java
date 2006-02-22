package org.objectweb.celtix.bus.transports.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE;

import org.mortbay.http.HttpRequest;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;
import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class HTTPClientTransport implements ClientTransport {

    //private static final Logger LOG = LogUtils.getL7dLogger(HTTPClientTransport.class);
    
    private static final String SERVICE_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/service-config";
    private static final String PORT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/port-config";
    private static final String HTTP_CLIENT_CONFIGURATION_URI =
        "http://celtix.objectweb.org/bus/transports/http/http-client-config";
    private static final String HTTP_CLIENT_CONFIGURATION_ID = "http-client";
        
    final HTTPClientPolicy policy;
    final AuthorizationPolicy authPolicy;
    final AuthorizationPolicy proxyAuthPolicy;
    final Configuration configuration;
    final EndpointReferenceType targetEndpoint;
    final Bus bus;
    final Port port;
    final HTTPTransportFactory factory;
    
    URL url;

      
    public HTTPClientTransport(Bus b, 
                               EndpointReferenceType ref, 
                               HTTPTransportFactory f) 
        throws WSDLException, IOException {

        bus = b;
        Configuration portConfiguration = getPortConfiguration(bus, ref);
        String address = portConfiguration.getString("address");
        EndpointReferenceUtils.setAddress(ref, address);
        targetEndpoint = ref;
        factory = f;
        url = new URL(address);
         
        port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        configuration = createConfiguration(portConfiguration);
        policy = getClientPolicy(configuration);
        authPolicy = getAuthPolicy("authorization", configuration);
        proxyAuthPolicy = getAuthPolicy("proxyAuthorization", configuration);
        
        
        bus.sendEvent(new ComponentCreatedEvent(this));
       
    }
    
    private HTTPClientPolicy getClientPolicy(Configuration conf) {
        HTTPClientPolicy pol = conf.getObject(HTTPClientPolicy.class, "httpClient");
        if (pol == null) {
            pol = new HTTPClientPolicy();
        }
        return pol;
    }
    private AuthorizationPolicy getAuthPolicy(String type, Configuration conf) {
        AuthorizationPolicy pol = conf.getObject(AuthorizationPolicy.class, type);
        if (pol == null) {
            pol = new AuthorizationPolicy();
        }
        return pol;
    }
       
    public EndpointReferenceType getTargetEndpoint() {
        return targetEndpoint;     
    }
    
    public EndpointReferenceType getDecoupledEndpoint() throws IOException {
        return factory.getDecoupledEndpoint(policy.getDecoupledEndpoint());
    }

    public Port getPort() {
        return port;
    }
    
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        return new HTTPClientOutputStreamContext(url, policy, authPolicy, proxyAuthPolicy, context);
    }

    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;
        ctx.flushHeaders();
    }
   
    public void invokeOneway(OutputStreamMessageContext context) throws IOException {
        HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;

        context.getOutputStream().close();
        ctx.createInputStreamContext().getInputStream().close();
    }

    public InputStreamMessageContext invoke(OutputStreamMessageContext context) throws IOException {
        context.getOutputStream().close();
        HTTPClientOutputStreamContext requestContext = (HTTPClientOutputStreamContext)context;
        return getResponseContext(requestContext, factory);
    }

    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, 
                                                         Executor executor) 
        throws IOException { 
        context.getOutputStream().close();
        HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;  
        FutureTask<InputStreamMessageContext> f = new FutureTask<InputStreamMessageContext>(
            new InputStreamMessageContextCallable(ctx, factory));
        // client (service) must always have an executor associated with it
        executor.execute(f);
        return f;
    }

    public void shutdown() {
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
        
        bus.sendEvent(new ComponentRemovedEvent(this));        
    }

    protected static InputStreamMessageContext getResponseContext(
                                 HTTPClientOutputStreamContext requestContext,
                                 HTTPTransportFactory factory) 
        throws IOException {
        InputStreamMessageContext responseContext = null;
        if (factory.hasDecoupledEndpoint()) {
            int responseCode = getResponseCode(requestContext.connection);
            if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                // server transport was rebased on decoupled response endpoint,
                // dispatch this partial response immediately as it may include
                // piggybacked content
                InputStreamMessageContext inputContext =
                    requestContext.createInputStreamContext();
                factory.getResponseCallback().dispatch(inputContext);
            } else {
                // request failed *before* server transport was rebased on
                // decoupled response endpoint
                responseContext = requestContext.createInputStreamContext();
            }   
        } else {
            responseContext = requestContext.createInputStreamContext();
        }
        return responseContext;
    }
    
    protected static Configuration getPortConfiguration(Bus bus, EndpointReferenceType ref) {
        Configuration busConfiguration = bus.getConfiguration();
        Configuration serviceConfiguration = busConfiguration
            .getChild(SERVICE_CONFIGURATION_URI,
                      EndpointReferenceUtils.getServiceName(ref).toString());
        Configuration portConfiguration = serviceConfiguration
            .getChild(PORT_CONFIGURATION_URI,
                      EndpointReferenceUtils.getPortName(ref));
        return portConfiguration;
    }
    
    private Configuration createConfiguration(Configuration portCfg) {
        ConfigurationBuilder cb = ConfigurationBuilderFactory.getBuilder(null);
        Configuration cfg = cb.getConfiguration(HTTP_CLIENT_CONFIGURATION_URI, 
                                                HTTP_CLIENT_CONFIGURATION_ID, 
                                                portCfg); 
        if (null == cfg) {
            cfg = cb.buildConfiguration(HTTP_CLIENT_CONFIGURATION_URI,
                                        HTTP_CLIENT_CONFIGURATION_ID,
                                        portCfg);            
        }
        // register the additional provider
        if (null != port) {
            cfg.getProviders().add(new WsdlHttpConfigurationProvider(port, false));
        }
        return cfg;
    }

    protected static int getResponseCode(URLConnection connection) throws IOException {
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
    
    protected static class HTTPClientOutputStreamContext
        extends MessageContextWrapper
        implements OutputStreamMessageContext {

        URLConnection connection;
        WrappedOutputStream origOut;
        OutputStream out;
        HTTPClientInputStreamContext inputStreamContext;
        HTTPClientPolicy policy;
        AuthorizationPolicy authPolicy;
        AuthorizationPolicy proxyAuthPolicy;

        @SuppressWarnings("unchecked")
        public HTTPClientOutputStreamContext(URL url,
                                             HTTPClientPolicy p,
                                             AuthorizationPolicy ap,
                                             AuthorizationPolicy pap,
                                             MessageContext ctx)
            throws IOException {
            super(ctx);

            Map<String, List<String>> headers = (Map<String, List<String>>)super.get(HTTP_REQUEST_HEADERS);
            if (null == headers) {
                headers = new HashMap<String, List<String>>();
                super.put(HTTP_REQUEST_HEADERS, headers);
            }

            policy = p;
            authPolicy = ap;
            proxyAuthPolicy = pap;
            String value = (String)ctx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            if (value != null) {
                url = new URL(value);
            }

            if (policy.isSetProxyServer()) {
                Proxy proxy = new Proxy(Proxy.Type.valueOf(policy.getProxyServerType().toString()),
                                        new InetSocketAddress(policy.getProxyServer(),
                                                              policy.getProxyServerPort()));
                connection = url.openConnection(proxy);
            } else {
                connection = url.openConnection();
            }
            connection.setDoOutput(true);

            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                hc.setRequestMethod("POST");
            }

            connection.setConnectTimeout((int)policy.getConnectionTimeout());
            connection.setReadTimeout((int)policy.getReceiveTimeout());

            connection.setUseCaches(false);
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                if (policy.isAutoRedirect()) {
                    //cannot use chunking if autoredirect as the request will need to be
                    //completely cached locally and resent to the redirect target
                    hc.setInstanceFollowRedirects(true);
                } else {
                    hc.setInstanceFollowRedirects(false);
                    if (policy.isAllowChunking()) {
                        hc.setChunkedStreamingMode(2048);                        
                    }
                }
            }
            setPolicies(headers);

            origOut = new WrappedOutputStream();
            out = origOut;
        }
        private void setPolicies(Map<String, List<String>> headers) {
            String userName = (String)get(BindingProvider.USERNAME_PROPERTY);
            if (userName == null && authPolicy.isSetUserName()) {
                userName = authPolicy.getUserName();
            }
            if (userName != null) {
                String passwd = (String)get(BindingProvider.PASSWORD_PROPERTY);
                if (passwd == null && authPolicy.isSetPassword()) {
                    passwd = authPolicy.getPassword();
                }
                userName += ":";
                if (passwd != null) {
                    userName += passwd;
                }
                userName = Base64Utility.encode(userName.getBytes());
                headers.put("Authorization",
                            Arrays.asList(new String[] {"Basic " + userName}));
            } else if (authPolicy.isSetAuthorizationType() && authPolicy.isSetAuthorization()) {
                String type = authPolicy.getAuthorizationType();
                type += " ";
                type += authPolicy.getAuthorization();
                headers.put("Authorization",
                            Arrays.asList(new String[] {type}));
            }
            if (proxyAuthPolicy.isSetUserName()) {
                userName = proxyAuthPolicy.getUserName();
                if (userName != null) {
                    String passwd = "";
                    if (proxyAuthPolicy.isSetPassword()) {
                        passwd = proxyAuthPolicy.getPassword();
                    }
                    userName += ":";
                    if (passwd != null) {
                        userName += passwd;
                    }
                    userName = Base64Utility.encode(userName.getBytes());
                    headers.put("Proxy-Authorization",
                                Arrays.asList(new String[] {"Basic " + userName}));
                } else if (proxyAuthPolicy.isSetAuthorizationType() && proxyAuthPolicy.isSetAuthorization()) {
                    String type = proxyAuthPolicy.getAuthorizationType();
                    type += " ";
                    type += proxyAuthPolicy.getAuthorization();
                    headers.put("Proxy-Authorization",
                                Arrays.asList(new String[] {type}));
                }
            }
            if (policy.isSetCacheControl()) {
                headers.put("Cache-Control",
                            Arrays.asList(new String[] {policy.getCacheControl().value()}));
            }
            if (policy.isSetHost()) {
                headers.put("Host",
                            Arrays.asList(new String[] {policy.getHost()}));
            }
            if (policy.isSetConnection()) {
                headers.put("Connection",
                            Arrays.asList(new String[] {policy.getConnection().value()}));                
            }
            if (policy.isSetAccept()) {
                headers.put("Accept",
                            Arrays.asList(new String[] {policy.getAccept()}));                
            }
            if (policy.isSetAcceptEncoding()) {
                headers.put("Accept-Encoding",
                            Arrays.asList(new String[] {policy.getAcceptEncoding()}));                
            }
            if (policy.isSetAcceptLanguage()) {
                headers.put("Accept-Language",
                            Arrays.asList(new String[] {policy.getAcceptLanguage()}));                
            }
            if (policy.isSetContentType()) {
                headers.put("Content-Type",
                            Arrays.asList(new String[] {policy.getContentType()}));                
            }
            if (policy.isSetCookie()) {
                headers.put("Cookie",
                            Arrays.asList(new String[] {policy.getCookie()}));                
            }
            if (policy.isSetBrowserType()) {
                headers.put("BrowserType",
                            Arrays.asList(new String[] {policy.getBrowserType()}));                
            }
            if (policy.isSetReferer()) {
                headers.put("Referer",
                            Arrays.asList(new String[] {policy.getReferer()}));                
            }
        }
        
        @SuppressWarnings("unchecked")
        void flushHeaders() throws IOException {
            Map<String, List<String>> headers = (Map<String, List<String>>)super.get(HTTP_REQUEST_HEADERS);
            if (null != headers) {
                for (String header : headers.keySet()) {
                    List<String> headerList = headers.get(header);
                    for (String string : headerList) {
                        connection.addRequestProperty(header, string);
                    }
                } 
            }
            origOut.resetOut(new BufferedOutputStream(connection.getOutputStream(), 1024));
        }

        public void setFault(boolean isFault) {
            //nothing to do
        }

        public boolean isFault() {
            return false;
        }
        
        public void setOneWay(boolean isOneWay) {
            put(ONEWAY_MESSAGE_TF, isOneWay);
        }
        
        public boolean isOneWay() {
            return ((Boolean)get(ONEWAY_MESSAGE_TF)).booleanValue();
        }
        
        public OutputStream getOutputStream() {
            return out;
        }

        public void setOutputStream(OutputStream o) {
            out = o;
        }

        public InputStreamMessageContext createInputStreamContext() throws IOException {
            if (inputStreamContext == null) {
                inputStreamContext =  new HTTPClientInputStreamContext(connection);
            }
            return inputStreamContext;
        }
        
        private class WrappedOutputStream extends FilterOutputStream {
            WrappedOutputStream() {
                super(new ByteArrayOutputStream());
            }
            void resetOut(OutputStream newOut) throws IOException {
                ByteArrayOutputStream bout = (ByteArrayOutputStream)out;
                if (bout.size() > 0) {
                    bout.writeTo(newOut);
                }
                out = newOut;
            }

            
            public void close() throws IOException {
                out.flush();
                if (inputStreamContext != null) {
                    inputStreamContext.initialise();
                }
            }
        }
    }

    static class HTTPClientInputStreamContext
        extends GenericMessageContext
        implements InputStreamMessageContext {

        private static final long serialVersionUID = 1L;
        
        final URLConnection connection;
        InputStream origInputStream;
        InputStream inStream;
        private boolean initialised; 

        public HTTPClientInputStreamContext(URLConnection con) throws IOException {
            connection = con;
            initialise();
        }
        
        /**
         * Calling getHeaderFields on the connection implicitly gets
         * the InputStream from the connection.  Getting the
         * InputStream implicitly closes the output stream which
         * renders it unwritable.  The InputStream context is created
         * before the binding is finished with it.  For this reason it
         * is necessary to initialise the InputStreamContext lazily.
         * When the OutputStream associated with this connection is
         * closed, it will invoke on this initialise method.  
         */
        void initialise()  throws IOException {
            if (!initialised) {
                put(ObjectMessageContext.MESSAGE_INPUT, false);
                put(HTTP_RESPONSE_HEADERS, connection.getHeaderFields());
                put(HTTP_RESPONSE_CODE, getResponseCode(connection));
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection hc = (HttpURLConnection)connection;
                    origInputStream = hc.getErrorStream();
                    if (null == origInputStream) {
                        origInputStream = connection.getInputStream();
                    }
                } else {
                    origInputStream = connection.getInputStream();
                }
            
                inStream = origInputStream;
                initialised = true;
            }
        } 

        public InputStream getInputStream() {
            try {
                initialise();
            } catch (IOException ex) { 
                throw new RuntimeException(ex); 
            } 
            return inStream;
        }

        public void setInputStream(InputStream ins) {
            inStream = ins;
        }
        
        public void setFault(boolean isFault) {
            //nothing to do
        }

        public boolean isFault() {
            assert get(HTTP_RESPONSE_CODE) != null;
            return ((Integer)get(HTTP_RESPONSE_CODE)).intValue() == 500;
        }
    }
    
    static class HTTPDecoupledClientInputStreamContext
        extends GenericMessageContext
        implements InputStreamMessageContext {

        InputStream inStream;
  
        public HTTPDecoupledClientInputStreamContext(HttpRequest decoupledResponse)  
            throws IOException {
            put(ObjectMessageContext.MESSAGE_INPUT, false);
            put(HTTP_RESPONSE_HEADERS, decoupledResponse.getParameters());
            put(HTTP_RESPONSE_CODE, HttpURLConnection.HTTP_ACCEPTED);
            inStream = drain(decoupledResponse.getInputStream());
        }

        public InputStream getInputStream() {
            return inStream;
        }

        public void setInputStream(InputStream ins) {
            inStream = ins;
        }
    
        public void setFault(boolean isFault) {
            //nothing to do
        }

        public boolean isFault() {
            return false;
        }
        
        private static InputStream drain(InputStream r) throws IOException {        
            byte[] bytes = new byte[4096];
            ByteArrayOutputStream w = new ByteArrayOutputStream();
            try {
                int offset = 0;
                int length = r.read(bytes, offset, bytes.length - offset);
                while (length != -1) {
                    offset += length;

                    if (offset == bytes.length) {
                        w.write(bytes, 0, bytes.length);
                        offset = 0;
                    }
                    
                    length = r.read(bytes, offset, bytes.length - offset);
                }
                if (offset != 0) {
                    w.write(bytes, 0, offset);
                }
            } finally {
                bytes = null;
            }
            return new ByteArrayInputStream(w.toByteArray());
        }
    }
    
    static class InputStreamMessageContextCallable implements Callable<InputStreamMessageContext> {
        private final HTTPClientOutputStreamContext httpClientOutputStreamContext;
        private final HTTPTransportFactory factory;
        
        InputStreamMessageContextCallable(HTTPClientOutputStreamContext ctx,
                                          HTTPTransportFactory f) {
            httpClientOutputStreamContext = ctx;
            factory = f;
        }
        public InputStreamMessageContext call() throws Exception {
            return getResponseContext(httpClientOutputStreamContext, factory);
        }   
    }
}
