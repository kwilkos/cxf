package org.objectweb.celtix.transports.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.http.HTTPClientTransport.HTTPClientInputStreamContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.WSAContextUtils;

public abstract class AbstractHTTPRequestorOutputStreamContext
    extends MessageContextWrapper
    implements OutputStreamMessageContext {

    URLConnection connection;
    WrappedOutputStream origOut;
    OutputStream out;
    HTTPClientInputStreamContext inputStreamContext;

    @SuppressWarnings("unchecked")
    public AbstractHTTPRequestorOutputStreamContext(MessageContext ctx)
        throws IOException {
        super(ctx);

        Map<String, List<String>> headers = (Map<String, List<String>>)super.get(HTTP_REQUEST_HEADERS);
        if (null == headers) {
            headers = new HashMap<String, List<String>>();
            super.put(HTTP_REQUEST_HEADERS, headers);
        }

        // TODO
        EndpointReferenceType to = WSAContextUtils.retrieveTo(null, ctx);
        if (to != null && to.getAddress() != null) {
            URL url = new URL(to.getAddress().getValue());
            connection = getConnection(url);
            connection.setDoOutput(true);
    
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                hc.setRequestMethod("POST");
            }
        }
        origOut = new WrappedOutputStream();
        out = origOut;
    }
    
    protected abstract URLConnection getConnection(URL url) throws IOException;

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
        connection.addRequestProperty("Content-Type", "text/xml");
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

    public InputStreamMessageContext getCorrespondingInputStreamContext() throws IOException {
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
