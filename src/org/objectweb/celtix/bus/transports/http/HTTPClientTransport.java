package org.objectweb.celtix.bus.transports.http;

import java.io.BufferedOutputStream;
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

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.configuration.Configuration;
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

    // private static final Logger LOG = LogUtils.getL7dLogger(HTTPClientTransport.class);
    final URL url;
    final Configuration configuration;
    final HTTPClientPolicy policy;
    final AuthorizationPolicy authPolicy;
    final AuthorizationPolicy proxyAuthPolicy;
      
    public HTTPClientTransport(Bus bus, EndpointReferenceType ref) throws WSDLException, IOException {

        Configuration portConfiguration = getPortConfiguration(bus, ref);
        String address = portConfiguration.getString("address");
        EndpointReferenceUtils.setAddress(ref, address);
        url = new URL(address);

        Port port =
            EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        configuration = 
            new HTTPClientTransportConfiguration(portConfiguration, port); 
        policy = getClientPolicy(configuration);
        authPolicy = getAuthPolicy("authorization", configuration);
        proxyAuthPolicy = getAuthPolicy("proxyAuthorization", configuration);
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
        return ((HTTPClientOutputStreamContext)context).createInputStreamContext();
    }

    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, 
                                                         Executor executor) 
        throws IOException { 
        context.getOutputStream().close();
        HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;  
        FutureTask<InputStreamMessageContext> f = new FutureTask<InputStreamMessageContext>(
            new InputStreamMessageContextCallable(ctx));
        // client (service) must always have an executor associated with it
        executor.execute(f);
        return f;
    }

    public void shutdown() {
        //nothing to do
    }
    
    private Configuration getPortConfiguration(Bus bus, EndpointReferenceType ref) {
        Configuration busConfiguration = bus.getConfiguration();
        Configuration serviceConfiguration = busConfiguration
            .getChild("http://celtix.objectweb.org/bus/jaxws/service-config",
                      EndpointReferenceUtils.getServiceName(ref));
        Configuration portConfiguration = serviceConfiguration
            .getChild("http://celtix.objectweb.org/bus/jaxws/port-config",
                      EndpointReferenceUtils.getPortName(ref));
        return portConfiguration;
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
                    hc.setChunkedStreamingMode(2048);
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
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection hc = (HttpURLConnection)connection;
                    put(HTTP_RESPONSE_CODE, hc.getResponseCode());
                
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
    
    static class InputStreamMessageContextCallable implements Callable<InputStreamMessageContext> {
        private final HTTPClientOutputStreamContext httpClientOutputStreamContext;
        
        InputStreamMessageContextCallable(HTTPClientOutputStreamContext ctx) {
            httpClientOutputStreamContext = ctx;
        }
        public InputStreamMessageContext call() throws Exception {
            return httpClientOutputStreamContext.createInputStreamContext();
        }   
    }
}
