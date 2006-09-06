/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.http;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.configuration.security.AuthorizationPolicy;
import org.apache.cxf.bus.configuration.security.SSLClientPolicy;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.message.Message;
import org.apache.cxf.oldcfg.CompoundName;
import org.apache.cxf.oldcfg.Configuration;
import org.apache.cxf.oldcfg.ConfigurationBuilder;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * Encapsulates all aspects of HTTP Conduit configuration.
 */
public class HTTPConduitConfiguration {
    private static final String HTTP_CLIENT_CONFIGURATION_URI =
        "http://cxf.apache.org/configuration/transport/http-client";
    private static final String HTTP_CLIENT_CONFIGURATION_ID = "http-client";

    final String address;
    final HTTPClientPolicy policy;
    final SSLClientPolicy sslClientPolicy;
    final AuthorizationPolicy authPolicy;
    final AuthorizationPolicy proxyAuthPolicy;
    final Configuration configuration;

    HTTPConduitConfiguration(Bus bus, EndpointInfo endpointInfo) {
        address = endpointInfo.getAddress();

        configuration = createConfiguration(bus, endpointInfo);
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
   
    private Configuration createConfiguration(Bus bus, EndpointInfo endpointInfo) {

        CompoundName id = new CompoundName(
            bus.getId(),
            endpointInfo.getService().getName().toString()
            + "/" + endpointInfo.getName().getLocalPart(),
            HTTP_CLIENT_CONFIGURATION_ID
        );
        ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
        Configuration cfg = cb.getConfiguration(HTTP_CLIENT_CONFIGURATION_URI, id);
        
        // register the additional provider

        cfg.getProviders().add(new ServiceModelHttpConfigurationProvider(endpointInfo, false));

        return cfg;
    }

}
