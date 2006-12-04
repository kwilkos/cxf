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

package org.apache.cxf.bus.cxf;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;

public class CXFBusFactory implements BusFactory {
    
    private static Bus defaultBus;

    public synchronized Bus getDefaultBus() {
        if (null == defaultBus) {
            defaultBus = new CXFBusImpl();
        }
        return defaultBus;
    }

    public void setDefaultBus(Bus bus) {
        defaultBus = bus;
    }
    
    public Bus createBus() {
        return createBus(new HashMap<Class, Object>());
    }
    
    public Bus createBus(Map<Class, Object> e) {
        return createBus(e, new HashMap<String, Object>());
    }
    
    public Bus createBus(Map<Class, Object> e, Map<String, Object> properties) {
        return new CXFBusImpl(e, properties);
    }
    
}
