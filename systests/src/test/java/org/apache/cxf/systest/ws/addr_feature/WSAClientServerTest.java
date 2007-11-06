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

package org.apache.cxf.systest.ws.addr_feature;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class WSAClientServerTest extends AbstractBusClientServerTestBase {

    private final QName serviceName = new QName("http://apache.org/cxf/systest/ws/addr_feature/",
                                                "AddNumbersService");
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    @Test
    public void testDetail() throws Exception {
        AddNumbersPortType port = getPort();

        //         JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        //         factory.setServiceClass(AddNumbersPortType.class);
        //         factory.setAddress("http://localhost:9090/jaxws/add");
        //         factory.getFeatures().add(new WSAddressingFeature());
        //         AddNumbersPortType port = (AddNumbersPortType) factory.create();
        assertEquals(3, port.addNumbers(1, 2));
    }
    
    private AddNumbersPortType getPort() {
        URL wsdl = getClass().getResource("/wsdl/add_numbers.wsdl");
        assertNotNull("WSDL is null", wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull("Service is null ", service);
        return service.getAddNumbersPort(new AddressingFeature());
    }
}
