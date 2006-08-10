package org.objectweb.celtix.transports.http;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

/**
 * Encapsulates all aspects of HTTP Conduit configuration.
 */
public class HTTPConduitConfiguration {
    private static final String PORT_CONFIGURATION_URI =
        "http://celtix.objectweb.org/bus/jaxws/port-config";
    private static final String HTTP_CLIENT_CONFIGURATION_URI =
        "http://celtix.objectweb.org/bus/transports/http/http-client-config";
    private static final String HTTP_CLIENT_CONFIGURATION_ID = "http-client";

    final String address;
    final Port port;
    final HTTPClientPolicy policy;
    final SSLClientPolicy sslClientPolicy;
    final AuthorizationPolicy authPolicy;
    final AuthorizationPolicy proxyAuthPolicy;
    final Configuration configuration;
    final Configuration portConfiguration;

    HTTPConduitConfiguration(Bus bus, EndpointReferenceType ref) throws WSDLException {
        port = EndpointReferenceUtils.getPort(bus.getExtension(WSDLManager.class), ref);
        portConfiguration = getPortConfiguration(bus, ref);
        address = portConfiguration.getString("address");
        EndpointReferenceUtils.setAddress(ref, address);
    
        configuration = createConfiguration(bus, portConfiguration);
        policy = getClientPolicy(configuration);
        authPolicy = getAuthPolicy("authorization", configuration);
        proxyAuthPolicy = getAuthPolicy("proxyAuthorization", configuration);
        sslClientPolicy = getSSLClientPolicy(configuration);
    }

    String getAddress() {
        return address;
    }
    
    HTTPClientPolicy getPolicy() {
        return policy;
    }

    SSLClientPolicy getSSLClientPolicy() {
        return sslClientPolicy;
    }
    
    AuthorizationPolicy getAuthPolicy() {
        return authPolicy;
    }
    
    AuthorizationPolicy getProxyAuthPolicy() {
        return proxyAuthPolicy;
    }

    Proxy getProxy() {
        Proxy proxy = null;
        if (policy.isSetProxyServer()) {
            proxy = new Proxy(Proxy.Type.valueOf(policy.getProxyServerType().toString()),
                              new InetSocketAddress(policy.getProxyServer(),
                                                    policy.getProxyServerPort()));
        }
        return proxy;
    }

    void setPolicies(Message message, Map<String, List<String>> headers) {
        String userName = (String)message.get(BindingProvider.USERNAME_PROPERTY);
        if (userName == null && authPolicy.isSetUserName()) {
            userName = authPolicy.getUserName();
        }
        if (userName != null) {
            String passwd = (String)message.get(BindingProvider.PASSWORD_PROPERTY);
            if (passwd == null && authPolicy.isSetPassword()) {
                passwd = authPolicy.getPassword();
            }
            userName += ":";
            if (passwd != null) {
                userName += passwd;
            }
            userName = Base64Utility.encode(userName.getBytes());
            headers.put("Authorization",
                        Arrays.asList(new String[] {"Basic " + userName}));
        } else if (authPolicy.isSetAuthorizationType() && authPolicy.isSetAuthorization()) {
            String type = authPolicy.getAuthorizationType();
            type += " ";
            type += authPolicy.getAuthorization();
            headers.put("Authorization",
                        Arrays.asList(new String[] {type}));
        }
        if (proxyAuthPolicy.isSetUserName()) {
            userName = proxyAuthPolicy.getUserName();
            if (userName != null) {
                String passwd = "";
                if (proxyAuthPolicy.isSetPassword()) {
                    passwd = proxyAuthPolicy.getPassword();
                }
                userName += ":";
                if (passwd != null) {
                    userName += passwd;
                }
                userName = Base64Utility.encode(userName.getBytes());
                headers.put("Proxy-Authorization",
                            Arrays.asList(new String[] {"Basic " + userName}));
            } else if (proxyAuthPolicy.isSetAuthorizationType() && proxyAuthPolicy.isSetAuthorization()) {
                String type = proxyAuthPolicy.getAuthorizationType();
                type += " ";
                type += proxyAuthPolicy.getAuthorization();
                headers.put("Proxy-Authorization",
                            Arrays.asList(new String[] {type}));
            }
        }
        if (policy.isSetCacheControl()) {
            headers.put("Cache-Control",
                        Arrays.asList(new String[] {policy.getCacheControl().value()}));
        }
        if (policy.isSetHost()) {
            headers.put("Host",
                        Arrays.asList(new String[] {policy.getHost()}));
        }
        if (policy.isSetConnection()) {
            headers.put("Connection",
                        Arrays.asList(new String[] {policy.getConnection().value()}));
        }
        if (policy.isSetAccept()) {
            headers.put("Accept",
                        Arrays.asList(new String[] {policy.getAccept()}));
        }
        if (policy.isSetAcceptEncoding()) {
            headers.put("Accept-Encoding",
                        Arrays.asList(new String[] {policy.getAcceptEncoding()}));
        }
        if (policy.isSetAcceptLanguage()) {
            headers.put("Accept-Language",
                        Arrays.asList(new String[] {policy.getAcceptLanguage()}));
        }
        if (policy.isSetContentType()) {
            headers.put("Content-Type",
                        Arrays.asList(new String[] {policy.getContentType()}));
        }
        if (policy.isSetCookie()) {
            headers.put("Cookie",
                        Arrays.asList(new String[] {policy.getCookie()}));
        }
        if (policy.isSetBrowserType()) {
            headers.put("BrowserType",
                        Arrays.asList(new String[] {policy.getBrowserType()}));
        }
        if (policy.isSetReferer()) {
            headers.put("Referer",
                        Arrays.asList(new String[] {policy.getReferer()}));
        }
    }

    
    private HTTPClientPolicy getClientPolicy(Configuration conf) {
        HTTPClientPolicy pol = conf.getObject(HTTPClientPolicy.class, "httpClient");
        if (pol == null) {
            pol = new HTTPClientPolicy();
        }
        return pol;
    }
    
    private AuthorizationPolicy getAuthPolicy(String type, Configuration conf) {
        AuthorizationPolicy pol = conf.getObject(AuthorizationPolicy.class, type);
        if (pol == null) {
            pol = new AuthorizationPolicy();
        }
        return pol;
    }
    
    private SSLClientPolicy getSSLClientPolicy(Configuration conf) {
        SSLClientPolicy pol = conf.getObject(SSLClientPolicy.class, "sslClient");
        if (pol == null) {
            pol = new SSLClientPolicy();
        }
        return pol;
    }
    
    private Configuration getPortConfiguration(Bus bus, EndpointReferenceType ref) {
        Configuration busConfiguration = bus.getConfiguration();
        String id = EndpointReferenceUtils.getServiceName(ref).toString()
            + "/" + EndpointReferenceUtils.getPortName(ref);
        Configuration portConfig = busConfiguration
            .getChild(PORT_CONFIGURATION_URI,
                      id);
                
        // REVISIT: the following should never be necessary 
 
        if (portConfig == null) {
            portConfig = bus.getConfiguration().getChild(PORT_CONFIGURATION_URI, id);
            if (null == portConfig) {
                ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
                portConfig = cb.buildConfiguration(PORT_CONFIGURATION_URI, id,
                                                   bus.getConfiguration());
            }

            // add the additional provider
            Port p = null;
            try  {
                p = EndpointReferenceUtils.getPort(bus.getExtension(WSDLManager.class), ref);
            } catch (WSDLException ex) {
                throw new WebServiceException("Could not get port from wsdl", ex);
            }
            portConfig.getProviders().add(new WsdlPortProvider(p));
        }        
        return portConfig;
    }

    private Configuration createConfiguration(Bus bus, Configuration portCfg) {
        Configuration cfg = portCfg.getChild(HTTP_CLIENT_CONFIGURATION_URI,
                                             HTTP_CLIENT_CONFIGURATION_ID);
        if (null == cfg) {
            ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
            cfg = cb.buildConfiguration(HTTP_CLIENT_CONFIGURATION_URI,
                                        HTTP_CLIENT_CONFIGURATION_ID,
                                        portCfg);
        }
        // register the additional provider
        if (null != port) {
            cfg.getProviders().add(new WsdlHttpConfigurationProvider(port, false));
        }
        return cfg;
    }

}
