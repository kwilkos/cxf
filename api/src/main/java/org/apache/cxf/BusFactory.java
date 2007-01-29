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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;

public abstract class BusFactory {
    
    public static final String BUS_FACTORY_PROPERTY_NAME = "org.apache.cxf.bus.factory";
    public static final String DEFAULT_BUS_FACTORY = "org.apache.cxf.bus.CXFBusFactory";

    private static final Logger LOG = LogUtils.getL7dLogger(BusFactory.class, "APIMessages");
    

    /** 
     * Creates a new bus. 
     * While concrete <code>BusFactory</code> may offer differently
     * parametrized methods for creating a bus, all factories support
     * this no-arg factory method.
     *
     * @return the newly created bus.
     */
    public abstract Bus createBus();
    
    /**
     * Returns the default bus, creating it if necessary.
     * 
     * @return the default bus.
     */
    public abstract Bus getDefaultBus();
    
    /**
     * Sets the default bus.
     * @param bus the default bus.
     */
    public abstract void setDefaultBus(Bus bus);    
    
    
    public static BusFactory newInstance() {
        BusFactory instance = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String className = getBusFactoryClass(null, classLoader);
        Class<? extends BusFactory> busFactoryClass;
        try {
            busFactoryClass = Class.forName(className, true, classLoader).asSubclass(BusFactory.class);
            instance = busFactoryClass.newInstance();
        } catch (Exception ex) {
            LogUtils.log(LOG, Level.SEVERE, "BUS_FACTORY_INSTANTIATION_EXC", ex);
        }
        return instance;
    }
    
    private static String getBusFactoryClass(Map<String, Object> properties, ClassLoader classLoader) {
        
        String busFactoryClass = null;
        
        // check properties  
        if (null != properties) {
            busFactoryClass = (String)properties.get(BusFactory.BUS_FACTORY_PROPERTY_NAME);
            if (isValidBusFactoryClass(busFactoryClass)) {
                return busFactoryClass;
            }
        }
        
        // next check system properties
        busFactoryClass = System.getProperty(BusFactory.BUS_FACTORY_PROPERTY_NAME);
        if (isValidBusFactoryClass(busFactoryClass)) {
            return busFactoryClass;
        }
    
        try {
            // next, check for the services stuff in the jar file
            String serviceId = "META-INF/services/" + BusFactory.BUS_FACTORY_PROPERTY_NAME;
            InputStream is = null;
        
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
        
            if (classLoader == null) {
                is = ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is = classLoader.getResourceAsStream(serviceId);
            }
            if (is != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                busFactoryClass = rd.readLine();
                rd.close();
            }
            if (isValidBusFactoryClass(busFactoryClass)) {
                return busFactoryClass;
            }

            // otherwise use default  
            busFactoryClass = BusFactory.DEFAULT_BUS_FACTORY;
            return busFactoryClass;
        } catch (Exception ex) {
            LogUtils.log(LOG, Level.SEVERE, "FAILED_TO_DETERMINE_BUS_FACTORY_EXC", ex);
        } 
        return busFactoryClass;
    }
    
    private static boolean isValidBusFactoryClass(String busFactoryClassName) { 
        return busFactoryClassName != null && !"".equals(busFactoryClassName);
    }
    
}
