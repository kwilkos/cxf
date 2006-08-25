package org.objectweb.celtix.transports.http.protocol.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PipeServer {
    static final Map<String, PipeHTTPServerTransport> SERVERS
        = new ConcurrentHashMap<String, PipeHTTPServerTransport>();
    
    private PipeServer() {
        //utility class
    }

    public static PipeResponse startDispatch(URL url, URLConnection connection,
                                             InputStream in,
                                             Map<String, List<String>> requestHeaders)
        throws IOException {
        
        PipeResponse resp = new PipeResponse(connection, in, requestHeaders);
        
        PipeHTTPServerTransport trans = SERVERS.get(url.getPath());
        if (trans == null) {
            throw new IOException("Could not connect to " + url.getPath());
        }
        trans.doService(resp);
        return resp;
    }
    
}
