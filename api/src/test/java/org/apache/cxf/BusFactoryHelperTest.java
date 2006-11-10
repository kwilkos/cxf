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

package org.apache.cxf;

import junit.framework.TestCase;



public class BusFactoryHelperTest extends TestCase {
    
    public void tearDown() {
        System.clearProperty(BusFactory.BUS_FACTORY_PROPERTY_NAME);        
    }
    
    public void testGetInstance() {
        BusFactory factory = BusFactoryHelper.newInstance();
        assertNull(factory);
        System.setProperty(BusFactory.BUS_FACTORY_PROPERTY_NAME, TestBusFactory.class.getName());
        factory = BusFactoryHelper.newInstance();
        assertTrue(factory instanceof TestBusFactory);
    }
    
    
    public static class TestBusFactory implements BusFactory {

        public Bus createBus() {
            return null;
        }

        public Bus getDefaultBus() {
            return null;
        }

        public void setDefaultBus(Bus bus) {         
        }
    }
}
