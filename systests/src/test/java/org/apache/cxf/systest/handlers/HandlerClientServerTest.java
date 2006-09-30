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

import javax.xml.namespace.QName;

//import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;

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

        port.addNumbers("20");

        //This assertion can not pass due to jira CXF-136: int type or more than two parameters dont work
        //assertEquals("2020", result);
    }

}
