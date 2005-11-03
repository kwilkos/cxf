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

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class HTTPClientTransport implements ClientTransport {
    final EndpointReferenceType reference;
    URL url;
    
    public HTTPClientTransport(Bus bus, EndpointReferenceType ref) throws WSDLException, IOException {
        reference = ref;
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        List<?> list = port.getExtensibilityElements();
        for (Object ep : list) {
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if (ext instanceof SOAPAddress) {
                SOAPAddress ad = (SOAPAddress)ext;
                url = new URL(ad.getLocationURI());
            }
        }
    }
    
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        return new HTTPClientOutputStreamContext(url, context);
    }

    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;
        ctx.flushHeaders();
    }

    
    
    public void invokeOneway(OutputStreamMessageContext context) throws IOException {
        HTTPClientOutputStreamContext ctx = (HTTPClientOutputStreamContext)context;
        ctx.createInputStreamContext().getInputStream().close();
    }

    public InputStreamMessageContext invoke(OutputStreamMessageContext context) throws IOException {
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

    
    static class HTTPClientOutputStreamContext
        extends MessageContextWrapper
        implements OutputStreamMessageContext {

        URLConnection connection;
        WrappedOutputStream origOut;
        OutputStream out;
        
        public HTTPClientOutputStreamContext(URL url, MessageContext ctx) throws IOException {
            super(ctx);
            connection =  url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                hc.setChunkedStreamingMode(4096);
                hc.setRequestMethod("POST");
            }
            // TODO - set connection timeouts, etc...
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
        
        public OutputStream getOutputStream() {
            return out;
        }

        public void setOutputStream(OutputStream o) {
            out = o;
        }

        public InputStreamMessageContext createInputStreamContext() throws IOException {
            return new HTTPClientInputStreamContext(connection);
        }
        
        private static class WrappedOutputStream extends FilterOutputStream {
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
        }
    }

    static class HTTPClientInputStreamContext
        extends GenericMessageContext
        implements InputStreamMessageContext {

        private static final long serialVersionUID = 1L;
        
        final URLConnection connection;
        InputStream origInputStream;
        InputStream inStream;
        
        public HTTPClientInputStreamContext(URLConnection con) throws IOException {
            connection = con;
            put(ObjectMessageContext.MESSAGE_INPUT, false);
            put(HTTP_RESPONSE_HEADERS, connection.getHeaderFields());
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)con;
                put(HTTP_RESPONSE_CODE, hc.getResponseCode());
                
                origInputStream = hc.getErrorStream();
                if (null == origInputStream) {
                    origInputStream = connection.getInputStream();
                }
            } else {
                origInputStream = connection.getInputStream();
            }
            
            inStream = origInputStream;
        }

        public InputStream getInputStream() {
            return inStream;
        }

        public void setInputStream(InputStream ins) {
            inStream = ins;
        }
        
        
    }
}
