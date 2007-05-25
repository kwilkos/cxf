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
package org.apache.cxf.transport.http_jetty;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.SSLServerPolicy;

public class JettyHTTPServerEngineFactory {

    /**
     * This map holds references for allocated ports.
     */
    // HACK!!! All system tests do not shut down bus correct,
    // or the bus does not shutdown all endpoints correctly,
    // so that these are shared amongst busses. Which is
    // hogwash!! This was static before I changed it, and I
    // tried to make it local.  Now, we get address in use
    // Bind exceptions because these server engines aren't
    // shared!! What hog wash. Propper shutdowns people!
    
    // We will keep it static until
    // we can resolve the problems in the System tests.
    // TODO: Fix the System Tests so that they shutdown the 
    // buses that they are using and that the buses actually
    // shutdown the destinations and their server engines
    // properly. This will require a bit of lifecyle and reference
    // counting on Destinations to server engines, if they are 
    // going to be shared, but they should by no means be 
    // shared accross buses, right?
    private static Map<Integer, JettyHTTPServerEngine> portMap =
        new HashMap<Integer, JettyHTTPServerEngine>();
    
    /**
     * The bus.
     */
    private Bus bus;
    
    protected JettyHTTPServerEngineFactory(Bus b) {
        bus = b;
    }
    
    /**
     * Allocate a JettyServer engine for a particular port. This call is allows 
     * the Spring configuration of the engine. If the protocol is "https" it 
     * must find a suitable configuration or this call will throw an error.
     */
    synchronized JettyHTTPServerEngine getForPort(String protocol, int p) {

        return getForPort(protocol, p, (TLSServerParameters) null);
    }

    /**
     * Allocate a Jetty server engine for a particular port, and an ssl 
     * server policy.
     * This call in order to remain consistent with previous implemenation 
     * does NOT override any spring configuration. That may be a bug. 
     * This method is deprecated in favor of using TLSServerParameters.
     */
    @Deprecated
    synchronized JettyHTTPServerEngine getForPort(
            String protocol,
            int p,
            SSLServerPolicy sslServerPolicy
    ) {
        JettyHTTPServerEngine ref = portMap.get(p);
        if (ref == null) {
            ref = new JettyHTTPServerEngine(this, bus, protocol, p);
            configure(ref);
            // This previous incantaion says programatic configuration does not 
            // override because init tests to see if sslServer is already set 
            // and if so, ignores this sslServerPolicy. 
            // This situation has been fixed with tlsServerParameters.
            ref.init(sslServerPolicy);
            ref.retrieveListenerFactory();
            portMap.put(p, ref);
        } else {
            // This will throw an exception if the reference cannot be 
            // reconfigured
            ref.reconfigure(protocol, sslServerPolicy);
        }
        return ref;
    }
    
    /**
     * Allocate a Jetty server engine for a particular port with TLS parameters.
     * If tlsParams is not null, it overrides any spring configuration of TLS 
     * parameters.
     */
    synchronized JettyHTTPServerEngine getForPort(
            String protocol,
            int p,
            TLSServerParameters tlsParams
    ) {
        JettyHTTPServerEngine ref = portMap.get(p);
        if (ref == null) {
            ref = new JettyHTTPServerEngine(this, bus, protocol, p);
            configure(ref);
            // Programatic configuration overrides Spring configuration.
            if (tlsParams != null) {
                ref.setProgrammaticTlsServerParameters(tlsParams);
            }
            try { 
                ref.finalizeConfig();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not initialize configuration of "
                        + ref.getBeanName() + ".", e);
            }
            portMap.put(p, ref);
        } else {
            // This call will throw an exception if the engine cannot be 
            // reconfigured.
            ref.reconfigure(protocol, tlsParams);
        }
        return ref;
    }
    
    /**
     * This method removes the Server Engine from the port map and stops it.
     */
    public synchronized void destroyForPort(int port) {
        JettyHTTPServerEngine ref = portMap.remove(port);
        if (ref != null) {
            try {
                ref.stop();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
    }

    /**
     * This call configures the Server Engine as Spring Bean.
     * @param bean
     */
    protected void configure(JettyHTTPServerEngine bean) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(bean);
        }
    }

    
}
