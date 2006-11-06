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

package org.apache.cxf.systest.ws.rm;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.GreeterService;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;


/**
 * Tests the addition of WS-RM properties to application messages and the 
 * exchange of WS-RM protocol messages.
 */
public class SequenceTest extends ClientServerTestBase {

    private Greeter greeter;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SequenceTest.class);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SequenceTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                // special case handling for WS-Addressing system test to avoid
                // UUID related issue when server is run as separate process
                // via maven on Win2k
                /*
                boolean inProcess = "Windows 2000".equals(System.getProperty("os.name"));
                assertTrue("server did not launch correctly", 
                           launchServer(Server.class, inProcess));
                */
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
            
            public void setUp() throws Exception {
                startServers();
                System.out.println("Started server");

                SpringBusFactory bf = new SpringBusFactory();
                Bus bus = bf.createBus("org/apache/cxf/systest/ws/rm/cxf.xml");
                bf.setDefaultBus(bus);
                setBus(bus);
                System.out.println("Created client bus");
            }
        };
    }

    public void setUp() throws Exception {
        super.setUp();
        GreeterService service = new GreeterService(); 
        System.out.println("Created GreeterService");
        greeter = service.getGreeterPort();
        System.out.println("Created Greeter");
    }
    
    public void tearDown() {
    }

    //--Tests
    
    public void testOneway() {
        System.out.println("Invoking greetMeOneWay ...");
        greeter.greetMeOneWay("cxf"); 
    }
}
