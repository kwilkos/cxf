package org.objectweb.celtix.connector;

import javax.resource.ResourceException;

/**
 * Interface implemented by the Web service client proxy returned by
 * {@link CeltixConnectionFactory}. It allows the caller to return the proxy to
 * the application server's pool when is no longer needed.
 */

public interface Connection {

    /**
     * close the connection handle. A caller should not use a closed connection.
     * 
     * @throws ResourceException if an error occurs during close.
     */
    void close() throws ResourceException;

}
