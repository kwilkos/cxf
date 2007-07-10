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



package org.apache.cxf.systest.soapheader;

import java.net.URL;

import javax.xml.namespace.QName;

import com.mypizzaco.pizza.PizzaPortType;
import com.mypizzaco.pizza.PizzaService;
import com.mypizzaco.pizza.types.CallerIDHeaderType;
import com.mypizzaco.pizza.types.OrderPizzaResponseType;
import com.mypizzaco.pizza.types.OrderPizzaType;
import com.mypizzaco.pizza.types.ToppingsListType;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class HeaderClientServerTest extends AbstractBusClientServerTestBase {

    private final QName serviceName = new QName("http://mypizzaco.com/pizza", "PizzaService");

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    @Test
    public void testBasicConnection() throws Exception {
        PizzaPortType port = getPort();

        OrderPizzaType req = new OrderPizzaType();
        ToppingsListType t = new ToppingsListType();
        t.getTopping().add("test");
        req.setToppings(t);

        CallerIDHeaderType header = new CallerIDHeaderType();
        header.setName("mao");
        header.setPhoneNumber("108");

        OrderPizzaResponseType res =  port.orderPizza(req, header);

        assertEquals(208, res.getMinutesUntilReady());
    }

    private PizzaPortType getPort() {
        URL wsdl = getClass().getResource("/wsdl/pizza_service.wsdl");
        assertNotNull("WSDL is null", wsdl);

        PizzaService service = new PizzaService(wsdl, serviceName);
        assertNotNull("Service is ull ", service);

        return service.getPizzaPort();
    }

}

