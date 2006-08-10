package org.objectweb.celtix.transports.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

/**
 * Encapsulates aspects of HTTP Destination configuration not related
 * to listening.
 */
public class HTTPDestinationConfiguration {
    private static final String ENDPOINT_CONFIGURATION_URI =
        "http://celtix.objectweb.org/bus/jaxws/endpoint-config";
    private static final String HTTP_SERVER_CONFIGURATION_URI =
        "http://celtix.objectweb.org/bus/transports/http/http-server-config";
    private static final String HTTP_SERVER_CONFIGURATION_ID = "http-server";
        

    protected String address;
    protected Configuration configuration;
    protected HTTPServerPolicy policy;
    
    public HTTPDestinationConfiguration(Bus bus, EndpointReferenceType ref)
        throws WSDLException, IOException {
        // get url (publish address) from endpoint reference
        address = EndpointReferenceUtils.getAddress(ref);  
        configuration = createConfiguration(bus, ref);
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
    
    private Configuration createConfiguration(Bus bus, EndpointReferenceType ref) {
        Configuration busConfiguration = bus.getConfiguration();
        QName serviceName = EndpointReferenceUtils.getServiceName(ref);
        Configuration endpointConfiguration = busConfiguration
            .getChild(ENDPOINT_CONFIGURATION_URI, serviceName.toString());
        Port port = null;
        try {
            port = EndpointReferenceUtils.getPort(bus.getExtension(WSDLManager.class), ref);
        } catch (WSDLException ex) {
            // ignore
        }
  
        Configuration cfg = endpointConfiguration.getChild(HTTP_SERVER_CONFIGURATION_URI, 
                                                HTTP_SERVER_CONFIGURATION_ID);
        if (null == cfg) {
            ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
            cfg = cb.buildConfiguration(HTTP_SERVER_CONFIGURATION_URI, 
                                        HTTP_SERVER_CONFIGURATION_ID, 
                                        endpointConfiguration);
        }
        // register the additional provider
        if (null != port) {
            cfg.getProviders().add(new WsdlHttpConfigurationProvider(port, true));
        }
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
