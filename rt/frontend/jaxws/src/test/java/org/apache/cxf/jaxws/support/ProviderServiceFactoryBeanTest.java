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
package org.apache.cxf.jaxws.support;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.binding.xml.XMLBinding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.AbstractJaxWsTest;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServerFactoryBean;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.hello_world_soap_http.HWSoapMessageProvider;

public class ProviderServiceFactoryBeanTest extends AbstractJaxWsTest {
    public void testFromWSDL() throws Exception {
        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        
        JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(HWSoapMessageProvider.class);
        ProviderServiceFactoryBean bean = new ProviderServiceFactoryBean(implInfo);

        // We should not have to do this, but otherwise Maven can't find the WSDL.
        // The classloader for ProviderServiecFactoryBean doesn't have the WSDL on it,
        // only the unit test does.
        bean.setWsdlURL(resource);
        
        Bus bus = getBus();
        bean.setBus(bus);
        bean.setServiceClass(HWSoapMessageProvider.class);

        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://apache.org/hello_world_soap_http", 
                     service.getName().getNamespaceURI());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        assertNotNull(intf);
        
        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setServiceFactory(bean);
        svrFactory.setStart(false);
        
        ServerImpl server = (ServerImpl) svrFactory.create();
        
        Endpoint endpoint = server.getEndpoint();
        Binding binding = endpoint.getBinding();
        assertTrue(binding instanceof SoapBinding);
    }
    
    public void testXMLBindingFromCode() throws Exception {
        JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(SourcePayloadProvider.class);
        ProviderServiceFactoryBean bean = new ProviderServiceFactoryBean(implInfo);

        Bus bus = getBus();
        bean.setBus(bus);

        Service service = bean.create();

        assertEquals("SourcePayloadProvider", service.getName().getLocalPart());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        assertNotNull(intf);
        
        assertEquals(1, service.getServiceInfo().getEndpoints().size());
        
        EndpointInfo ei = service.getServiceInfo().getEndpoint(new QName("SourcePayloadProviderPort"));
        
        assertNotNull(ei);
        
        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setServiceFactory(bean);
        
        ServerImpl server = (ServerImpl) svrFactory.create();
        Endpoint endpoint = server.getEndpoint();
        Binding binding = endpoint.getBinding();
        assertTrue(binding instanceof XMLBinding);
    }
}

