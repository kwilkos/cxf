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

package org.apache.cxf.management.jmx;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.cxf.BusException;
import org.apache.cxf.configuration.instrumentation.types.JMXConnectorPolicyType;
import org.apache.cxf.configuration.instrumentation.types.MBServerPolicyType;
import org.apache.cxf.management.jmx.export.AnnotationTestInstrumentation;


public class JMXManagedComponentManagerTest extends TestCase {
       
    private static final String NAME_ATTRIBUTE = "Name";    
    private JMXManagedComponentManager manager;
    
    public void setUp() throws BusException {
        manager = new JMXManagedComponentManager(); 
    }
        
    public void testJMXManagerInit() {
        MBServerPolicyType policy = new MBServerPolicyType();
        JMXConnectorPolicyType connector = new JMXConnectorPolicyType();        
        policy.setJMXConnector(connector);        
        connector.setDaemon(false);
        connector.setThreaded(true);
        connector.setJMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9913/jmxrmi");
        try {
            manager.init(policy); 
            Thread.sleep(300);
            manager.shutdown();
        } catch (Exception ex) {
            assertTrue("JMX Manager init with NewMBeanServer error", false);
            ex.printStackTrace();
        }
    }
    
    public void testRegisterInstrumentation() {
        MBServerPolicyType policy = new MBServerPolicyType();
        JMXConnectorPolicyType connector = new JMXConnectorPolicyType();        
        policy.setJMXConnector(connector);        
        connector.setDaemon(false);
        connector.setThreaded(false);
        connector.setJMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9913/jmxrmi");
        manager.init(policy);
        // setup the fack instrumentation
        AnnotationTestInstrumentation im = new AnnotationTestInstrumentation();
        ObjectName name = JMXUtils.getObjectName(im.getUniqueInstrumentationName(), 
                                                 im.getInstrumentationName());
       
        im.setName("John Smith");          
        manager.registerMBean(im);
        
        try {            
            Object val = manager.getMBeanServer().getAttribute(name, NAME_ATTRIBUTE);
            assertEquals("Incorrect result", "John Smith", val);
            Thread.sleep(300);
        } catch (Exception ex) {            
            ex.printStackTrace();
            assertTrue("get instrumentation attribute error", false);
        }
        manager.unregisterMBean(im);
        manager.shutdown();
    }

}
