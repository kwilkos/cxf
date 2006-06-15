package org.objectweb.celtix.bus.transports.http.protocol.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PipeURLConnection extends URLConnection {

    Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
    PipedOutputStream pout;
    PipeResponse response;
    
    public PipeURLConnection(URL url) {
        super(url);
    }

    public void connect() throws IOException {
        PipedInputStream pin = new BiggerPipeInputStream();
        pout = new PipedOutputStream(pin);
        response = PipeServer.startDispatch(url, this, pin, requestHeaders);
        connected = true;
    }
    
    public InputStream getInputStream() throws IOException {
        return response.getInputStream(getReadTimeout());
    }

    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            connect();
        }
        return pout;
    }

    private void setupReceive() throws IOException {
        if (pout != null) {
            pout.flush();
            pout.close();
            pout = null;
        }
    }
    
    
    //request properties support
    public void setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        requestHeaders.put(key, Arrays.asList(new String[] {value}));
    }
    public void addRequestProperty(String key, String value) {
        super.addRequestProperty(key, value);
        List<String> list = requestHeaders.get(key);
        if (null == list) {
            setRequestProperty(key, value);
        } else {
            list.add(value);
        }
    }
    public String getRequestProperty(String key) {
        super.getRequestProperty(key);
        List<String> list = requestHeaders.get(key);
        if (list != null && !list.isEmpty()) {
            if (list.size() == 1) {
                return list.get(0);
            } else {
                StringBuffer buf = new StringBuffer(list.get(0));
                for (int x = 1; x < list.size(); x++) {
                    buf.append(";");
                    buf.append(list.get(x));
                }
                return buf.toString();
            }
        }
        return null;
    }
    public Map<String, List<String>> getRequestProperties() {
        return Collections.unmodifiableMap(requestHeaders);
    }
    
    
    
    //response fields
    public Map<String, List<String>> getHeaderFields() {
        try {
            setupReceive();
            return Collections.unmodifiableMap(response.getResponseHeaders(getReadTimeout()));
        } catch (IOException e) {
            return null;
        }
    }
    public String getHeaderField(String name) {
        try {
            setupReceive();
            List<String> list = response.getResponseHeaders(getReadTimeout()).get(name);
            if (list != null && !list.isEmpty()) {
                if (list.size() == 1) {
                    return list.get(0);
                } else {
                    StringBuffer buf = new StringBuffer(list.get(0));
                    for (int x = 1; x < list.size(); x++) {
                        buf.append(";");
                        buf.append(list.get(x));
                    }
                    return buf.toString();
                }
            }
        } catch (IOException e) {
            //ignore
        }
        return null;
    }    
    public String getHeaderFieldKey(int n) {
        int x = 0;
        Set<String> keys;
        try {
            setupReceive();
            keys = response.getResponseHeaders(getReadTimeout()).keySet();
        } catch (IOException e) {
            return null;
        }
        for (String name : keys) {
            if (x == n) {
                return name;
            }
            ++x;
        }
        return null;
    }
    public String getHeaderField(int n) {
        int x = 0;
        Set<String> keys;
        try {
            setupReceive();
            keys = response.getResponseHeaders(getReadTimeout()).keySet();
        } catch (IOException e) {
            return null;
        }
        for (String name : keys) {
            if (x == n) {
                return getHeaderField(name);
            }
            ++x;
        }
        return null;
    }



    private static class BiggerPipeInputStream extends PipedInputStream {
        BiggerPipeInputStream() {
            super();
            buffer = new byte[PIPE_SIZE * 4];
        }
    }
}
