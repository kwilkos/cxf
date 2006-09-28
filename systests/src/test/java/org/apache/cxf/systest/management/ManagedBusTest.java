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

package org.apache.cxf.systest.management;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.cxf.CXFBusFactory;
import org.apache.cxf.configuration.Configurer;

public class ManagedBusTest extends TestCase {

    public void testManagedBus() {
        CXFBusFactory factory = new CXFBusFactory();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Configurer.USER_CFG_FILE_PROPERTY_NAME, 
                       "org/apache/cxf/systest/management/managed.xml");
        Bus bus = factory.createBus(null, properties);
        factory.setDefaultBus(bus);
        // bus.run();
    }
}
