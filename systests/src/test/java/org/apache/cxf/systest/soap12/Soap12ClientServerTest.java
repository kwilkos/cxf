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



package org.apache.cxf.systest.soap12;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.hello_world_soap12_http.Greeter;
import org.apache.hello_world_soap12_http.PingMeFault;
import org.apache.hello_world_soap12_http.SOAPService;
import org.apache.hello_world_soap12_http.types.FaultDetail;

public class Soap12ClientServerTest extends ClientServerTestBase {    

    private final QName serviceName = new QName("http://apache.org/hello_world_soap12_http",
                                                "SOAPService");
    private final QName portName = new QName("http://apache.org/hello_world_soap12_http", "SoapPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(Soap12ClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
        
    }

    public void testBasicConnection() throws Exception {
        Greeter greeter = getGreeter();
        for (int i = 0; i < 5; i++) {
            String echo = greeter.sayHi();
            assertEquals("Bonjour", echo);
        }

    }

    public void testPingMeFault() throws Exception {
        Greeter greeter = getGreeter();
        try {
            greeter.pingMe();
            fail("Should throw Exception!");
        } catch (PingMeFault ex) {
            FaultDetail detail = ex.getFaultInfo();
            assertEquals((short)2, detail.getMajor());
            assertEquals((short)1, detail.getMinor());
            assertEquals("PingMeFault raised by server", ex.getMessage());            
        }
    }

    private Greeter getGreeter() {
        URL wsdl = getClass().getResource("/wsdl/hello_world_soap12.wsdl");
        assertNotNull("WSDL is null", wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull("Service is ull ", service);

        return service.getPort(portName,
                               Greeter.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Soap12ClientServerTest.class);
    }
}

