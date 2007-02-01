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

package org.apache.cxf.systest.jaxws;

import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.hello_world_mixedstyle.Greeter;
import org.apache.hello_world_mixedstyle.SOAPService;
import org.apache.hello_world_mixedstyle.types.GreetMe1;
import org.apache.hello_world_mixedstyle.types.GreetMeResponse;

public class ClientServerMixedStyleTest extends TestCase {

    private final QName portName = new QName("http://apache.org/hello_world_mixedstyle", "SoapPort");

  
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClientServerMixedStyleTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(ServerMixedStyle.class));
            }
        };
    }
    
    public void testMixedStyle() throws Exception {

        SOAPService service = new SOAPService();
        assertNotNull(service);

        try {
            Greeter greeter = service.getPort(portName, Greeter.class);
            
            GreetMe1 request = new GreetMe1();
            request.setRequestType("Bonjour");
            GreetMeResponse greeting = greeter.greetMe(request);
            assertNotNull("no response received from service", greeting);
            assertEquals("Hello Bonjour", greeting.getResponseType());

            String reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals("Bonjour", reply);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

}
