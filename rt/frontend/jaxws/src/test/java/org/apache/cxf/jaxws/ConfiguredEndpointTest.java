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

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.cxf.CXFBusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.support.JaxwsEndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.GreeterImpl;
import org.apache.hello_world_soap_http.SOAPService;

public class ConfiguredEndpointTest extends TestCase {
    private static final QName SERVICE_NAME = 
        new QName("http://apache.org/hello_world_soap_http", "SOAPService");    
    private static final QName PORT_NAME = 
        new QName("http://apache.org/hello_world_soap_http", "SoapPort");

    private CXFBusFactory factory;
    
    public void setUp() {
        factory = new CXFBusFactory();
        Bus bus = factory.getDefaultBus();
        if (null != bus) {
            bus.shutdown(true);
            factory.setDefaultBus(null);
        }
    }
    
    public void tearDown() {
        Bus bus = factory.getDefaultBus();
        if (null != bus) {
            bus.shutdown(true);
            factory.setDefaultBus(null);
        }
    }
     
    public void testDefaultClientEndpoint() {        
        factory.setDefaultBus(factory.createBus());
        
        javax.xml.ws.Service service = new SOAPService();
        Greeter greeter = service.getPort(PORT_NAME, Greeter.class);
        
        EndpointInvocationHandler eih = (EndpointInvocationHandler)Proxy.getInvocationHandler(greeter);
        Client client = eih.getClient();
        JaxwsEndpointImpl endpoint = (JaxwsEndpointImpl)client.getEndpoint();
        assertEquals("Unexpected bean name", PORT_NAME.toString(), endpoint.getBeanName());
        assertTrue("Unexpected value for property validating", !endpoint.getValidating());
   
        List<Interceptor> interceptors = endpoint.getInInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = endpoint.getOutInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = endpoint.getInFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = endpoint.getOutFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        
        org.apache.cxf.service.ServiceImpl svc = (org.apache.cxf.service.ServiceImpl)endpoint.getService();
        assertEquals("Unexpected bean name", SERVICE_NAME.toString(), svc.getBeanName());
        interceptors = svc.getInInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = svc.getOutInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = svc.getInFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = svc.getOutFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
    }
    
    @SuppressWarnings("unchecked")
    public void testConfiguredClientEndpoint() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Configurer.USER_CFG_FILE_PROPERTY_NAME, 
                       "org/apache/cxf/jaxws/configured-endpoints.xml");
        factory.setDefaultBus(factory.createBus(null, properties));
        
        javax.xml.ws.Service service = new SOAPService();
        Greeter greeter = service.getPort(PORT_NAME, Greeter.class);

        EndpointInvocationHandler eih = (EndpointInvocationHandler)Proxy.getInvocationHandler(greeter);
        Client client = eih.getClient();
        JaxwsEndpointImpl endpoint = (JaxwsEndpointImpl)client.getEndpoint();
        assertEquals("Unexpected bean name", PORT_NAME.toString(), endpoint.getBeanName());
        // assertTrue("Unexpected value for property validating", endpoint.getValidating());
        List<Interceptor> interceptors = endpoint.getInInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-in", 
                     findTestInterceptor(interceptors).getId());
        interceptors = endpoint.getOutInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-out", 
                     findTestInterceptor(interceptors).getId());
        interceptors = endpoint.getInFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-in-fault", 
                     findTestInterceptor(interceptors).getId());
        interceptors = endpoint.getOutFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-out-fault", 
                     findTestInterceptor(interceptors).getId());
        
        org.apache.cxf.service.ServiceImpl svc = (org.apache.cxf.service.ServiceImpl)endpoint.getService();
        assertEquals("Unexpected bean name.", SERVICE_NAME.toString(), svc.getBeanName());
        interceptors = svc.getInInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-in", 
                     findTestInterceptor(interceptors).getId());
        interceptors = svc.getOutInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-out", 
                     findTestInterceptor(interceptors).getId());
        interceptors = svc.getInFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-in-fault", 
                     findTestInterceptor(interceptors).getId());
        interceptors = svc.getOutFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-out-fault", 
                     findTestInterceptor(interceptors).getId());
    }
    
    public void testDefaultServerEndpoint() {
        factory.setDefaultBus(factory.createBus());
        
        Object implementor = new GreeterImpl(); 
        EndpointImpl ei = (EndpointImpl)(javax.xml.ws.Endpoint.create(implementor));
        
        JaxwsEndpointImpl endpoint = (JaxwsEndpointImpl)ei.getEndpoint();
        assertEquals("Unexpected bean name", PORT_NAME.toString(), endpoint.getBeanName());
        assertTrue("Unexpected value for property validating", !endpoint.getValidating());
   
        List<Interceptor> interceptors = endpoint.getInInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = endpoint.getOutInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = endpoint.getInFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = endpoint.getOutFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        
        org.apache.cxf.service.ServiceImpl svc = (org.apache.cxf.service.ServiceImpl)endpoint.getService();
        assertEquals("Unexpected bean name", SERVICE_NAME.toString(), svc.getBeanName());
        interceptors = svc.getInInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = svc.getOutInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = svc.getInFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
        interceptors = svc.getOutFaultInterceptors();
        assertNull("Unexpected test interceptor", findTestInterceptor(interceptors));
    }
    
    @SuppressWarnings("unchecked")
    public void testConfiguredServerEndpoint() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Configurer.USER_CFG_FILE_PROPERTY_NAME, 
                       "org/apache/cxf/jaxws/configured-endpoints.xml");
        factory.setDefaultBus(factory.createBus(null, properties));
        
        Object implementor = new GreeterImpl(); 
        EndpointImpl ei = (EndpointImpl)(javax.xml.ws.Endpoint.create(implementor));
        
        JaxwsEndpointImpl endpoint = (JaxwsEndpointImpl)ei.getEndpoint();
        assertEquals("Unexpected bean name", PORT_NAME.toString(), endpoint.getBeanName());
        assertTrue("Unexpected value for property validating", endpoint.getValidating());
        List<Interceptor> interceptors = endpoint.getInInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-in", 
                     findTestInterceptor(interceptors).getId());
        interceptors = endpoint.getOutInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-out", 
                     findTestInterceptor(interceptors).getId());
        interceptors = endpoint.getInFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-in-fault", 
                     findTestInterceptor(interceptors).getId());
        interceptors = endpoint.getOutFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "endpoint-out-fault", 
                     findTestInterceptor(interceptors).getId());
        
        org.apache.cxf.service.ServiceImpl svc = (org.apache.cxf.service.ServiceImpl)endpoint.getService();
        assertEquals("Unexpected bean name.", SERVICE_NAME.toString(), svc.getBeanName());
        interceptors = svc.getInInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-in", 
                     findTestInterceptor(interceptors).getId());
        interceptors = svc.getOutInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-out", 
                     findTestInterceptor(interceptors).getId());
        interceptors = svc.getInFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-in-fault", 
                     findTestInterceptor(interceptors).getId());
        interceptors = svc.getOutFaultInterceptors();
        assertEquals("Unexpected number of interceptors.", 1, interceptors.size());
        assertEquals("Unexpected interceptor id.", "service-out-fault", 
                     findTestInterceptor(interceptors).getId());
    }
      
    private AbstractPhaseInterceptor findTestInterceptor(List<Interceptor> interceptors) {
        for (Interceptor i : interceptors) {
            if (i instanceof TestInterceptor) {
                return (TestInterceptor)i;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    static final class TestInterceptor extends AbstractPhaseInterceptor {
    
        public void handleMessage(Message message) throws Fault {
            // TODO Auto-generated method stub
        }
        
        public void setName(String n) {
            setId(n);
        }
        
    }
}
