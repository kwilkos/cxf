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
package org.apache.cxf.jaxws;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.calculator.CalculatorPortType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.NullConduitSelector;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.SOAPService;
import org.junit.Ignore;
import org.junit.Test;

public class ServiceImplTest extends AbstractJaxWsTest {

    private static final QName SERVICE_1 = 
        new QName("http://apache.org/cxf/calculator", "CalculatorService");

    private static final QName PORT_1 = 
        new QName("http://apache.org/cxf/calculator", "CalculatorPort");
    
    private static final QName SOAP_PORT =
        new QName("http://apache.org/hello_world_soap_http", "SoapPort");

    @Test
    public void testServiceImpl() throws Exception {
        SOAPService service = new SOAPService();
        
        Greeter proxy = service.getSoapPort();
        
        Client client = ClientProxy.getClient(proxy);
        assertEquals("bar", client.getEndpoint().get("foo"));
        assertNotNull("expected ConduitSelector", client.getConduitSelector());
        assertTrue("unexpected ConduitSelector",
                   client.getConduitSelector() instanceof NullConduitSelector);
    }
    
    @Test
    @Ignore
    public void testNonSpecificGetPort() throws Exception {
        SOAPService service = new SOAPService();
        
        Greeter proxy = service.getPort(Greeter.class);
        
        Client client = ClientProxy.getClient(proxy);
        assertEquals("unexpected port selected",
                     SOAP_PORT,
                     client.getEndpoint().getEndpointInfo().getName());
        assertEquals("bar", client.getEndpoint().get("foo"));
        assertNotNull("expected ConduitSelector", client.getConduitSelector());
        assertTrue("unexpected ConduitSelector",
                   client.getConduitSelector() instanceof NullConduitSelector);
    }
    
    @Override
    protected Bus createBus() throws BusException {
        SpringBusFactory bf = new SpringBusFactory();
        return bf.createBus("/org/apache/cxf/jaxws/soapServiceConfig.xml");
    }

    @Test
    public void testBadServiceName() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        QName badService = 
            new QName("http://apache.org/cxf/calculator", "DoesNotExist");
        
        try {
            new ServiceImpl(getBus(), wsdl1, badService, ServiceImpl.class);
            fail("Did not throw exception");
        } catch (WebServiceException e) {
            // that's expected
        }
    }
    
    @Test
    public void testPorts() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);
        Iterator iter = service.getPorts();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(PORT_1, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testGetBadPort() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);

        QName badPort = 
            new QName("http://apache.org/cxf/calculator", "PortDoesNotExist");
        try {
            service.getPort(badPort, CalculatorPortType.class);
            fail("Did not throw expected exception");
        } catch (WebServiceException e) {
            // that's ok
        }
    }

    @Test
    public void testGetBadSEI() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);

        try {
            service.getPort(ServiceImpl.class);
            fail("Did not throw expected exception");
        } catch (WebServiceException e) {
            // that's ok
        }
    }

    @Test
    public void testGetGoodPort() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);

        CalculatorPortType cal = (CalculatorPortType)service.getPort(PORT_1, CalculatorPortType.class);
        assertNotNull(cal);
    }

    @Test
    public void testCreateDispatchGoodPort() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);

        Dispatch dispatch = service.createDispatch(PORT_1, Source.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
    }

    @Test
    public void testCreateDispatchBadPort() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);

        QName badPort = 
            new QName("http://apache.org/cxf/calculator", "PortDoesNotExist");
        try {
            service.createDispatch(badPort, Source.class, Service.Mode.PAYLOAD);
        } catch (WebServiceException e) {
            // that's ok
        }
    }
    
    @Test
    public void testHandlerResolver() {
        URL wsdl1 = getClass().getResource("/wsdl/calculator.wsdl");
        assertNotNull(wsdl1);
        
        ServiceImpl service = new ServiceImpl(getBus(), wsdl1, SERVICE_1, ServiceImpl.class);

        TestHandlerResolver resolver = new TestHandlerResolver();
        assertNull(resolver.getPortInfo());

        service.setHandlerResolver(resolver);
        
        CalculatorPortType cal = (CalculatorPortType)service.getPort(PORT_1, CalculatorPortType.class);
        assertNotNull(cal);

        PortInfo info = resolver.getPortInfo();
        assertNotNull(info);
        assertEquals(SERVICE_1, info.getServiceName());
        assertEquals(PORT_1, info.getPortName());
        assertEquals(SOAPBinding.SOAP12HTTP_BINDING, info.getBindingID());
    }

    private static class TestHandlerResolver implements HandlerResolver {
        private PortInfo info;

        public PortInfo getPortInfo() {
            return info;
        }

        public List<Handler> getHandlerChain(PortInfo portInfo) {
            List<Handler> handlerList = new ArrayList<Handler>();
            this.info = portInfo;
            return handlerList;
        }
    }
    
}
