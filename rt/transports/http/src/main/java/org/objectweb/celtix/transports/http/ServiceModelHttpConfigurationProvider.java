package org.apache.cxf.transports.http;

import org.apache.cxf.configuration.ConfigurationProvider;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;

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
