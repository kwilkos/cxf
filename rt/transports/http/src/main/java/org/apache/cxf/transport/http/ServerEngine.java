package org.apache.cxf.transport.http;

import java.net.URL;

import org.mortbay.http.HttpHandler;
import org.mortbay.http.handler.AbstractHttpHandler;

public interface ServerEngine {
    /**
     * Register a servant.
     * 
     * @param url the URL associated with the servant
     * @param handler notified on incoming HTTP requests
     */
    void addServant(URL url, AbstractHttpHandler handler);
    
    /**
     * Remove a previously registered servant.
     * 
     * @param url the URL the servant was registered against.
     */
    void removeServant(URL url);
    
    /**
     * Get a previously  registered servant.
     * 
     * @param url the associated URL
     * @return the HttpHandler if registered
     */
    HttpHandler getServant(URL url);
}
