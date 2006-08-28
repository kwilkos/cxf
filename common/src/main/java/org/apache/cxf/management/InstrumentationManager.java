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

package org.apache.cxf.management;

import java.util.List;

import javax.management.MBeanServer;

/** 
 *  InstrumentationManager interface for the instrumentations query, register 
 *  and unregister
 */
public interface InstrumentationManager {
    
    /**
     * register the instrumentation instance to the instrumentation manager      
     */
    void register(Instrumentation instrumentation);

    /**
     * unregister the instrumentation instance from the instrumentation manager  
     */
    void unregister(Object component);

    /**
     * get all instrumentation from the instrumentation manager
     * @return the instrumentation list 
     */
    List<Instrumentation> getAllInstrumentation();

    /**
     * provide a clean up method for instrumentation manager to stop
     */
    void shutdown();
    
    /**
     * get the MBeanServer which will host the cxf runtime component MBeans
     * NOTE: if the configuration is not set the JMXEnabled to be true, this method
     * will return null
     * @return the MBeanServer 
     */
    MBeanServer getMBeanServer();

}
