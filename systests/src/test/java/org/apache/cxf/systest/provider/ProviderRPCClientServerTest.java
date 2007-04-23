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

package org.apache.cxf.systest.provider;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.hello_world_rpclit.GreeterRPCLit;
import org.apache.hello_world_rpclit.SOAPServiceRPCLit;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProviderRPCClientServerTest extends AbstractBusClientServerTestBase {

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    private void doGreeterRPCLit(SOAPServiceRPCLit service, QName portName, int count) throws Exception {
        String response1 = new String("TestGreetMeResponse");
        String response2 = new String("TestSayHiResponse");
        try {
            GreeterRPCLit greeter = service.getPort(portName, GreeterRPCLit.class);
            for (int idx = 0; idx < count; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                assertEquals(response1, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    @Test
    public void testSOAPMessageModeRPC() throws Exception {

        QName serviceName = new QName("http://apache.org/hello_world_rpclit", "SOAPServiceProviderRPCLit");
        QName portName = new QName("http://apache.org/hello_world_rpclit", "SoapPortProviderRPCLit1");

        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        doGreeterRPCLit(service, portName, 2);
    }

    @Test
    public void testSOAPMessageModeWithDOMSourceData() throws Exception {
        QName serviceName = new QName("http://apache.org/hello_world_rpclit", "SOAPServiceProviderRPCLit");
        QName portName = new QName("http://apache.org/hello_world_rpclit", "SoapPortProviderRPCLit2");

        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        doGreeterRPCLit(service, portName, 2);
    }

    @Test
    public void testPayloadModeWithDOMSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = new QName("http://apache.org/hello_world_rpclit", "SOAPServiceProviderRPCLit");
        QName portName = new QName("http://apache.org/hello_world_rpclit", "SoapPortProviderRPCLit3");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        doGreeterRPCLit(service, portName, 1);
    }

    @Test
    public void testMessageModeWithSAXSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = new QName("http://apache.org/hello_world_rpclit", "SOAPServiceProviderRPCLit");
        QName portName = new QName("http://apache.org/hello_world_rpclit", "SoapPortProviderRPCLit4");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        doGreeterRPCLit(service, portName, 1);
    }

    @Test
    public void testMessageModeWithStreamSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = new QName("http://apache.org/hello_world_rpclit", "SOAPServiceProviderRPCLit");
        QName portName = new QName("http://apache.org/hello_world_rpclit", "SoapPortProviderRPCLit5");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        doGreeterRPCLit(service, portName, 1);
    }

    @Test
    public void testPayloadModeWithSAXSourceData() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull(wsdl);

        QName serviceName = new QName("http://apache.org/hello_world_rpclit", "SOAPServiceProviderRPCLit");
        QName portName = new QName("http://apache.org/hello_world_rpclit", "SoapPortProviderRPCLit6");

        SOAPServiceRPCLit service = new SOAPServiceRPCLit(wsdl, serviceName);
        assertNotNull(service);

        doGreeterRPCLit(service, portName, 1);
    }

}
