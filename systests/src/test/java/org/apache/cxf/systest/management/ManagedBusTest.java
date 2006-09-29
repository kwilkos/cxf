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
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.InstrumentationManagerImpl;
import org.apache.cxf.management.InstrumentationType;

public class ManagedBusTest extends TestCase {

    public void xtestManagedCXFBus() {
        CXFBusFactory factory = new CXFBusFactory();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Configurer.USER_CFG_FILE_PROPERTY_NAME, 
                       "org/apache/cxf/systest/management/managed-cxf.xml");
        Bus bus = factory.createBus(null, properties);
        bus.shutdown(true);
    }

    public void testManagedSpringBus() {
        SpringBusFactory factory = new SpringBusFactory();
        Bus bus = factory.createBus("org/apache/cxf/systest/management/managed-spring.xml", true);
        InstrumentationManager im = bus.getExtension(InstrumentationManager.class);
        assertNotNull(im);
        InstrumentationManagerImpl imi = (InstrumentationManagerImpl)im;
        InstrumentationType i = imi.getInstrumentation();
        assertNotNull(i);
        System.out.println("isEnabled: " + i.isEnabled());
        System.out.println("isJMXEnabled: " + i.isJMXEnabled());
        


        bus.shutdown(true);
    }
}
