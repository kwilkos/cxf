package org.objectweb.celtix.transports.http;

import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

public class ServiceModelHttpConfigurationProvider implements ConfigurationProvider {

    private final EndpointInfo info;
    private final boolean server;

    public ServiceModelHttpConfigurationProvider(EndpointInfo i,  boolean s) {
        info = i;
        server = s;
    }

    public Object getObject(String name) {
        if (null == info) {
            return null;
        }

        if (server && "httpServer".equals(name)) {
            return info.getExtensor(HTTPServerPolicy.class);
        }
        
        if (!server && "httpClient".equals(name)) {
            return info.getExtensor(HTTPClientPolicy.class);
        }

        return null;
    }

    /**
     * TODO
     */
    public boolean setObject(String name, Object value) {
        return false;
    }
    

    public boolean save() {
        //TODO:
        return false;
    }
    
}
