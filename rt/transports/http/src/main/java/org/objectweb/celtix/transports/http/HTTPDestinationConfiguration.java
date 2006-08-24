package org.apache.cxf.transports.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.CompoundName;
import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.configuration.ConfigurationBuilder;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;

/**
 * Encapsulates aspects of HTTP Destination configuration not related
 * to listening.
 */
public class HTTPDestinationConfiguration {
    private static final String HTTP_SERVER_CONFIGURATION_URI =
        "http://cxf.apache.org/configuration/transport/http-server";
    private static final String HTTP_SERVER_CONFIGURATION_ID = "http-server";
        

    protected String address;
    protected Configuration configuration;
    protected HTTPServerPolicy policy;
    
    public HTTPDestinationConfiguration(Bus bus, EndpointInfo endpointInfo) throws IOException {
        // get url (publish address) from endpoint info
        address = endpointInfo.getAddress();
        configuration = createConfiguration(bus, endpointInfo);
        policy = getServerPolicy(configuration);
    }
    
    String getAddress() {
        return address;
    }
    
    HTTPServerPolicy getPolicy() {
        return policy; 
    }
    
    boolean contextMatchOnStem() {
        return "stem".equals(configuration.getString("contextMatchStrategy"));
    }
    
    void setPolicies(Map<String, List<String>> headers) {
        if (policy.isSetCacheControl()) {
            headers.put("Cache-Control",
                        Arrays.asList(new String[] {policy.getCacheControl().value()}));
        }
        if (policy.isSetContentLocation()) {
            headers.put("Content-Location",
                        Arrays.asList(new String[] {policy.getContentLocation()}));
        }
        if (policy.isSetContentEncoding()) {
            headers.put("Content-Encoding",
                        Arrays.asList(new String[] {policy.getContentEncoding()}));
        }
        if (policy.isSetContentType()) {
            headers.put("Content-Type",
                        Arrays.asList(new String[] {policy.getContentType()}));
        }
        if (policy.isSetServerType()) {
            headers.put("Server",
                        Arrays.asList(new String[] {policy.getServerType()}));
        }
        if (policy.isSetHonorKeepAlive() && !policy.isHonorKeepAlive()) {
            headers.put("Connection",
                        Arrays.asList(new String[] {"close"}));
        }
        
    /*
     * TODO - hook up these policies
    <xs:attribute name="SuppressClientSendErrors" type="xs:boolean" use="optional" default="false">
    <xs:attribute name="SuppressClientReceiveErrors" type="xs:boolean" use="optional" default="false">
    */
    }
    
    private Configuration createConfiguration(Bus bus, EndpointInfo endpointInfo) {
        
        CompoundName id = new CompoundName(
            bus.getId(),
            endpointInfo.getName().toString(),
            HTTP_SERVER_CONFIGURATION_ID
        );

        ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
        Configuration cfg = cb.getConfiguration(HTTP_SERVER_CONFIGURATION_URI, id);

        // create and register the additional provider

        cfg.getProviders().add(new ServiceModelHttpConfigurationProvider(endpointInfo, true));

        return cfg;
    }
    
    private HTTPServerPolicy getServerPolicy(Configuration conf) {
        HTTPServerPolicy pol = conf.getObject(HTTPServerPolicy.class, "httpServer");
        if (pol == null) {
            pol = new HTTPServerPolicy();
        }
        return pol;
    }

}
