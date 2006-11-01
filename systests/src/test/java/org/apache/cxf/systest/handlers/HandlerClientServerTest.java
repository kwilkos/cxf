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

package org.apache.cxf.systest.handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

//import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.handlers.AddNumbers;
import org.apache.handlers.AddNumbersService;

public class HandlerClientServerTest extends ClientServerTestBase {

    static QName serviceName = new QName("http://apache.org/handlers", "AddNumbersService");

    static QName portName = new QName("http://apache.org/handlers", "AddNumbersPort");

    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new AddNumbersImpl();
            String address = "http://localhost:9025/handlers/AddNumbersService/AddNumbersPort";
            Endpoint.publish(address, implementor);
        }

        public static void main(String[] args) {
            try {
                Server s = new Server();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                System.out.println("done!");
            }
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(HandlerClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testInvokeLogicalHandler() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        AddNumbers port = (AddNumbers)service.getPort(portName, AddNumbers.class);
        
        //Add client side handlers programmatically
        SmallNumberHandler sh = new SmallNumberHandler();
        List<Handler> newHandlerChain = new ArrayList<Handler>();
        newHandlerChain.add(sh);
        ((BindingProvider)port).getBinding().setHandlerChain(newHandlerChain);

        int result = port.addNumbers(10, 20);
        assertEquals(200, result);
        int result1 = port.addNumbers(5, 6);
        //TODO: This test can not pass due to jira cxf-195
        //assertEquals(11, result1);
    }

}
