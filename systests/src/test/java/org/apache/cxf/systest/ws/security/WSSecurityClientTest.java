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

package org.apache.cxf.systest.ws.security;

import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;

import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.hello_world_soap_http.Greeter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
public class WSSecurityClientTest extends AbstractBusClientServerTestBase {
    
    private static final java.net.URL WSDL_LOC;
    static {
        java.net.URL tmp = null;
        try {
            tmp = WSSecurityClientTest.class.getClassLoader().getResource(
                "org/apache/cxf/systest/ws/security/hello_world.wsdl"
            );
        } catch (final Exception e) {
            e.printStackTrace();
        }
        WSDL_LOC = tmp;
    }
    
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue(
            "Server failed to launch",
            // run the server in the same process
            // set this to false to fork
            launchServer(Server.class, true)
        );
    }
    
    @Test
    @Ignore
    public void testTimestampSignEncrypt() {
        BusFactory.setDefaultBus(
            new SpringBusFactory().createBus(
                "org/apache/cxf/systest/ws/security/client.xml"
            )
        );
        final javax.xml.ws.Service svc = javax.xml.ws.Service.create(
            WSDL_LOC,
            new javax.xml.namespace.QName(
                "http://apache.org/hello_world_soap_http",
                "SOAPServiceWSSecurity"
            )
        );
        final Greeter greeter = svc.getPort(
            new javax.xml.namespace.QName(
                "http://apache.org/hello_world_soap_http",
                "TimestampSignEncrypt"
            ),
            Greeter.class
        );
        greeter.sayHi();
    }
}
