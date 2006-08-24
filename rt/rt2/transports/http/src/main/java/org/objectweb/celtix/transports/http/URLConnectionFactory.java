package org.objectweb.celtix.transports.http;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * Encapsulates creation of URLConnection.
 */
public interface URLConnectionFactory {

    /**
     * Create a URLConnection, proxified if neccessary.
     * 
     * @param proxy non-null if connection should be proxified
     * @param url the target URL
     * @return an appropriate URLConnection
     */
    URLConnection createConnection(Proxy proxy, URL url) throws IOException;
}
