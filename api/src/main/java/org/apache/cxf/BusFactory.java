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

public interface BusFactory {
    
    String BUS_FACTORY_PROPERTY_NAME = "org.apache.cxf.bus.factory";
    String DEFAULT_BUS_FACTORY = "org.apache.cxf.bus.CXFBusFactory";
    
    /** 
     * Creates a new bus. 
     * While concrete <code>BusFactory</code> may offer differently
     * parametrized methods for creating a bus, all factories support
     * this no-arg factory method.
     *
     * @return the newly created bus.
     */
    Bus createBus();
    
    /**
     * Returns the default bus, creating it if necessary.
     * 
     * @return the default bus.
     */
    Bus getDefaultBus();
    
    /**
     * Sets the default bus.
     * @param bus the default bus.
     */
    void setDefaultBus(Bus bus);    
}
