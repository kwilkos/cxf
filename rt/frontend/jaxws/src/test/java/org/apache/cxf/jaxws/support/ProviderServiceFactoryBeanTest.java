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

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.BindingFactoryManagerImpl;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.apache.hello_world_soap_http.HWSoapMessageProvider;
import org.easymock.IMocksControl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class ProviderServiceFactoryBeanTest extends TestCase {
    public void testFromWSDL() throws Exception {
        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        
        JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(HWSoapMessageProvider.class);
        ProviderServiceFactoryBean bean = new ProviderServiceFactoryBean(implInfo);

        // We should not have to do this, but otherwise Maven can't find the WSDL.
        // The classloader for ProviderServiecFactoryBean doesn't have the WSDL on it,
        // only the unit test does.
        bean.setWsdlURL(resource);
        
        Bus bus = createBus();
        bean.setBus(bus);
        bean.setServiceClass(HWSoapMessageProvider.class);

        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://apache.org/hello_world_soap_http", 
                     service.getName().getNamespaceURI());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        assertNotNull(intf);
    }
    
    public void testXMLBindingFromCode() throws Exception {
        JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(SourcePayloadProvider.class);
        ProviderServiceFactoryBean bean = new ProviderServiceFactoryBean(implInfo);

        Bus bus = createBus();
        bean.setBus(bus);

        Service service = bean.create();

        assertEquals("SourcePayloadProvider", service.getName().getLocalPart());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        assertNotNull(intf);
        
        assertEquals(1, service.getServiceInfo().getEndpoints().size());
        
        EndpointInfo ei = service.getServiceInfo().getEndpoint(new QName("SourcePayloadProviderPort"));
        
        assertNotNull(ei);
    }

    Bus createBus() throws Exception {
        IMocksControl control = createNiceControl();
        Bus bus = control.createMock(Bus.class);

        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        BindingFactoryManager bfm = new BindingFactoryManagerImpl();
        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bfm).anyTimes();

        WSDLManagerImpl wsdlMan = new WSDLManagerImpl();
        expect(bus.getExtension(WSDLManager.class)).andReturn(wsdlMan);

        control.replay();

        return bus;
    }
}

