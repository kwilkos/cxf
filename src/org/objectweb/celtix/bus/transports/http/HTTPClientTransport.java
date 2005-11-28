package org.objectweb.celtix.bus.transports.http;



import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.wsdl.WSDLException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class HTTPClientTransport implements ClientTransport {
    
    URL url;
    Configuration configuration;
      
    public HTTPClientTransport(Bus bus, EndpointReferenceType ref) throws WSDLException, IOException {
        
        Configuration portConfiguration = getPortConfiguration(bus, ref);
        url = new URL(portConfiguration.getString("address"));
        
        configuration = 
            new HTTPClientTransportConfiguration(portConfiguration, 
                                                 EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref));
    }
    
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        return new HTTPClientOutputStreamContext(url, configuration, context);
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

    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context) 
        throws IOException {
        
        //HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;
        // TODO async return stuff
        return null;
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

    
    static class HTTPClientOutputStreamContext
        extends MessageContextWrapper
        implements OutputStreamMessageContext {

        URLConnection connection;
        WrappedOutputStream origOut;
        OutputStream out;
        HTTPClientInputStreamContext inputStreamContext;
        Configuration config;

        public HTTPClientOutputStreamContext(URL url, Configuration configuration, MessageContext ctx)
            throws IOException {
            super(ctx);
            config = configuration;
            String value = (String)ctx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            if (value != null) {
                url = new URL(value);
            }
            
            connection = url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                hc.setChunkedStreamingMode(4096);
                hc.setRequestMethod("POST");
            }
            
            value = (String)ctx.get(BindingProvider.USERNAME_PROPERTY);
            if (value != null) {
                String passwd = (String)ctx.get(BindingProvider.PASSWORD_PROPERTY);
                value += ":";
                if (passwd != null) {
                    value += passwd;
                }
                value = Base64Utility.encode(value.getBytes());
                connection.addRequestProperty("Authorization", "Basic " + value);
            }
            
            // TODO - set connection timeouts, etc...
            
            // connection.setReadTimeout(configuration.getInt(""));
            
            origOut = new WrappedOutputStream();
            out = origOut;
        }
        
        void flushHeaders() throws IOException {
            Map<?, ?> headers = (Map<?, ?>)super.get(HTTP_REQUEST_HEADERS);
            if (null != headers) {
                for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                    String header = (String)iter.next();
                    List<?> headerList = (List<?>)headers.get(header);
                    for (Object string : headerList) {
                        connection.addRequestProperty(header, (String)string);
                    }
                } 
            }
            origOut.resetOut(connection.getOutputStream());
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
            return (boolean) ((Boolean)get(ONEWAY_MESSAGE_TF)).booleanValue();
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
        private void initialise()  throws IOException {
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
}
