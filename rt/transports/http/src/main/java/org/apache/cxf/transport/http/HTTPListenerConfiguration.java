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

import org.apache.cxf.Bus;
import org.apache.cxf.bus.configuration.security.SSLServerPolicy;
import org.apache.cxf.oldcfg.CompoundName;
import org.apache.cxf.oldcfg.Configuration;
import org.apache.cxf.oldcfg.ConfigurationBuilder;
import org.apache.cxf.transports.http.configuration.HTTPListenerPolicy;

/**
 * Encapsulates aspects of HTTP Destination configuration related
 * to listening (this is separated from the main destination config
 * as a servlet-based destination does not require an explicit listener).
 */
public class HTTPListenerConfiguration {
    private static final String HTTP_LISTENER_CONFIGURATION_URI = 
        "http://cxf.apache.org/configuration/transport/http-listener";
    
    private Configuration config;
    private HTTPListenerPolicy policy;
    private SSLServerPolicy sslPolicy;
    
    public HTTPListenerConfiguration(Bus bus, String protocol, int port) {
        config = createConfiguration(bus, port);
        policy = config.getObject(HTTPListenerPolicy.class, "httpListener");
        sslPolicy = config.getObject(SSLServerPolicy.class, "sslServer");
        if (sslPolicy == null && "https".equals(protocol)) {
            sslPolicy = new SSLServerPolicy();
        }
    }
    
    HTTPListenerPolicy getPolicy() {
        return policy;
    }
    
    SSLServerPolicy getSSLPolicy() {
        return sslPolicy;
    }
    
    private Configuration createConfiguration(Bus bus, int p) {

        // REVISIT

        CompoundName id = new CompoundName(bus.getId(), "http-listener." + p);
        
        ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
        return cb.getConfiguration(HTTP_LISTENER_CONFIGURATION_URI, id);
    }
}
