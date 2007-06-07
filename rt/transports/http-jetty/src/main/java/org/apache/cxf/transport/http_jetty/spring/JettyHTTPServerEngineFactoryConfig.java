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
package org.apache.cxf.transport.http_jetty.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.jsse.spring.TLSServerParametersConfig;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.cxf.transport.http_jetty.ThreadingParameters;
import org.apache.cxf.transports.http_jetty.configuration.JettyHTTPServerEngineConfigType;
import org.apache.cxf.transports.http_jetty.configuration.JettyHTTPServerEngineFactoryConfigType;
import org.apache.cxf.transports.http_jetty.configuration.TLSServerParametersIdentifiedType;
import org.apache.cxf.transports.http_jetty.configuration.ThreadingParametersIdentifiedType;
import org.apache.cxf.transports.http_jetty.configuration.ThreadingParametersType;

public class JettyHTTPServerEngineFactoryConfig {
    Map<Integer, TLSServerParameters> tlsParametersMap =
        new TreeMap<Integer, TLSServerParameters>();
    Map<Integer, ThreadingParameters> threadingParametersMap =
        new TreeMap<Integer, ThreadingParameters>();
        
    
    JettyHTTPServerEngineFactoryConfig(
        JettyHTTPServerEngineFactoryConfigType config
    ) {
        Map<String, TLSServerParameters> tlsMap =
            new HashMap<String, TLSServerParameters>();
        Map<String, ThreadingParameters> threadingMap =
            new HashMap<String, ThreadingParameters>();
        for (ThreadingParametersIdentifiedType t 
                : config.getIdentifiedThreadingParameters()) {
            if (threadingMap.get(t.getId()) != null) {
                throw new RuntimeException("Threading Parameters " + t.getId()
                        + " is configured more than once.");
            }
            threadingMap.put(t.getId(), 
                    toThreadingParameters(t.getThreadingParameters()));
        }
        for (TLSServerParametersIdentifiedType t 
                : config.getIdentifiedTLSServerParameters()) {
            if (tlsMap.get(t.getId()) != null) {
                throw new RuntimeException("TLS Server Parameters " + t.getId()
                        + " is configured more than once.");
            }
            try {
                TLSServerParametersConfig con = 
                    new TLSServerParametersConfig(t.getTlsServerParameters());
                tlsMap.put(t.getId(), con);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not configure TLS in " + t.getId(), e);
            }
        }
        for (JettyHTTPServerEngineConfigType t : config.getEngine()) {
            ThreadingParameters tparams = null;
            if (t.getThreadingParametersRef() != null) {
                String id = t.getThreadingParametersRef().getId();
                tparams = threadingMap.get(id);
                if (tparams == null) {
                    throw new RuntimeException("Could not find \"" 
                            + id + "\" as threading parameters");
                }
            } else if (t.getThreadingParameters() != null) {
                tparams = toThreadingParameters(t.getThreadingParameters());
            }
            TLSServerParameters tlsParams = null;
            if (t.getTlsServerParametersRef() != null) {
                String id = t.getTlsServerParametersRef().getId();
                tlsParams = tlsMap.get(id);
                if (tlsParams == null) {
                    throw new RuntimeException("Could not find \"" 
                            + id + "\" as TLS Server Parameters");
                }
            } else if (t.getTlsServerParameters() != null) {
                try {
                    tlsParams = 
                        new TLSServerParametersConfig(t.getTlsServerParameters());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Could not configure TLS for port " + t.getPort(), e);
                }
            }
            if (threadingParametersMap.get(t.getPort()) != null
                || tlsParametersMap.get(t.getPort()) != null) {
                throw new RuntimeException("Port " + t.getPort()
                        + " is configured more than once");
            }
            if (tparams != null) {
                threadingParametersMap.put(t.getPort(), tparams);
            }
            if (tlsParams != null) {
                tlsParametersMap.put(t.getPort(), tlsParams);
            }
        }
    }
    
    private ThreadingParameters toThreadingParameters(
            ThreadingParametersType paramtype
    ) {
        ThreadingParameters params = new ThreadingParameters();
        params.setMaxThreads(paramtype.getMaxThreads());
        params.setMinThreads(paramtype.getMinThreads());
        return params;
    }

    public void configureServerEngineFactory(JettyHTTPServerEngineFactory fac) {
        for (int port : tlsParametersMap.keySet()) {
            fac.setTLSServerParametersForPort(port, 
                    tlsParametersMap.get(port));
        }
        for (int port : threadingParametersMap.keySet()) {
            fac.setThreadingParametersForPort(port, 
                    threadingParametersMap.get(port));
        }
    }
}
