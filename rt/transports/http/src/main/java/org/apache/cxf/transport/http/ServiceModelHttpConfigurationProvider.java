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

import org.apache.cxf.oldcfg.ConfigurationProvider;
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
