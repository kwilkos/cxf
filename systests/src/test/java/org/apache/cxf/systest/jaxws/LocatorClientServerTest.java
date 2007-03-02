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

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;


import junit.framework.Test;
import junit.framework.TestSuite;



import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;



import org.apache.locator.LocatorService;
import org.apache.locator.LocatorService_Service;
import org.apache.locator.query.QuerySelectType;
import org.apache.locator_test.LocatorServiceImpl;

public class LocatorClientServerTest extends ClientServerTestBase {

    static final Logger LOG = Logger.getLogger(LocatorClientServerTest.class.getName());
    private final QName serviceName = new QName("http://apache.org/locator", "LocatorService");

    public static class MyServer extends TestServerBase {

        protected void run() {
            Object implementor = new LocatorServiceImpl();
            String address = "http://localhost:6006/services/LocatorService";
            Endpoint.publish(address, implementor);

        }

        public static void main(String[] args) {
            try {
                MyServer s = new MyServer();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                LOG.info("done!");
            }
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(LocatorClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(MyServer.class));
            }
        };

    }

    public void testLocatorService() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/locator.wsdl");
        assertNotNull(wsdl);

        LocatorService_Service ss = new LocatorService_Service(wsdl, serviceName);
        LocatorService port = ss.getLocatorServicePort();

        
        port.registerPeerManager(new org.apache.cxf.ws.addressing.EndpointReferenceType(),
                                 new Holder<org.apache.cxf.ws.addressing.EndpointReferenceType>(),
                                 new Holder<java.lang.String>());

        port.deregisterPeerManager(new java.lang.String());

        
        port.registerEndpoint(null, new org.apache.cxf.ws.addressing.EndpointReferenceType());

        
        port.deregisterEndpoint(null, new org.apache.cxf.ws.addressing.EndpointReferenceType());

        
        
        port.lookupEndpoint(new javax.xml.namespace.QName("", ""));
            
        port.listEndpoints();

        port.queryEndpoints(new QuerySelectType());

    }
}
