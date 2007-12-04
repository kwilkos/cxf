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

package org.apache.cxf.systest.ws.addr_fromjava;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.systest.ws.addr_fromjava.client.AddNumberImpl;
import org.apache.cxf.systest.ws.addr_fromjava.client.AddNumberImplService;
import org.apache.cxf.systest.ws.addr_fromjava.server.Server;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WSAFromJavaTest extends AbstractBusClientServerTestBase {

    @Before
    public void setUp() throws Exception {
        createBus();
    }

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    private ByteArrayOutputStream setupInLogging() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos, true);
        LoggingInInterceptor in = new LoggingInInterceptor(writer);
        this.bus.getInInterceptors().add(in);
        return bos;
    }

    private ByteArrayOutputStream setupOutLogging() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos, true);

        LoggingOutInterceptor out = new LoggingOutInterceptor(writer);
        this.bus.getOutInterceptors().add(out);

        return bos;
    }

    @Test
    public void testAddNumbers() throws Exception {
        ByteArrayOutputStream input = setupInLogging();
        ByteArrayOutputStream output = setupOutLogging();

        AddNumberImpl port = getPort();

        assertEquals(3, port.addNumbers(1, 2));

        String expectedOut = "http://cxf.apache.org/input";
        assertTrue(output.toString().indexOf(expectedOut) != -1);
        
        // TODO: will support response action after the java2wsdl support the wsa:action
        String expectedIn = "http://cxf.apache.org/output";
        assertTrue(input.toString().indexOf(expectedIn) == -1);
    }

    private AddNumberImpl getPort() {
        URL wsdl = getClass().getResource("/wsdl/add_numbers-fromjava.wsdl");
        assertNotNull("WSDL is null", wsdl);

        AddNumberImplService service = new AddNumberImplService(wsdl);
        assertNotNull("Service is null ", service);

        // TODO, this is wrong, the addressing could be enabled by reading the wsdl extensions
        return service.getAddNumberImplPort(new AddressingFeature());
    }
}