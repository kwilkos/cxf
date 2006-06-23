package org.objectweb.celtix.transports.http.protocol.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class PipeResponse {
    Map<String, List<String>> reqHeaders;
    InputStream reqIn;
    URLConnection connection;
    
    Map<String, List<String>> headers;
    InputStream in;
    OutputStream pout;

    public PipeResponse(URLConnection con,
                        InputStream in2,
                        Map<String, List<String>> requestHeaders) {
        connection = con;
        reqIn = in2;
        reqHeaders = requestHeaders;
    }
    public URLConnection getURLConnection() {
        return connection;
    }
    public Map<String, List<String>> getRequestHeaders() {
        return reqHeaders;
    }
    public InputStream getRequestInputStream() {
        return reqIn;
    }
    
    public OutputStream setResponse(Map<String, List<String>> h) throws IOException {
        synchronized (this) {
            PipedInputStream pin = new BiggerPipeInputStream();
            pout = new PipedOutputStream(pin);
            headers = h;
            in = pin;
            notifyAll();
            return pout;
        }
    }
    public OutputStream getOutputStream() {
        return pout;
    }
    
    public Map<String, List<String>> getResponseHeaders(int timeout) 
        throws IOException {
        synchronized (this) {
            if (headers == null) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                    //ignore
                }
                if (headers == null) {
                    throw new IOException("Timeout getting response");
                }
            }
            return headers;
        }
    }
    
    public InputStream getInputStream(int timeout) throws IOException {
        getResponseHeaders(timeout);
        return in;
    }      


    private static class BiggerPipeInputStream extends PipedInputStream {
        BiggerPipeInputStream() {
            super();
            buffer = new byte[PIPE_SIZE * 4];
        }
    }
}

