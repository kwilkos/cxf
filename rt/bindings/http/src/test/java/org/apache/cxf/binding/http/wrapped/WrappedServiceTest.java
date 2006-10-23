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
package org.apache.cxf.binding.http.wrapped;

import org.w3c.dom.Document;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.http.AbstractRestTest;
import org.apache.cxf.binding.http.HttpBindingFactory;
import org.apache.cxf.binding.http.HttpBindingInfoFactoryBean;
import org.apache.cxf.binding.http.URIMapper;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServerFactoryBean;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.http.JettyHTTPDestination;

public class WrappedServiceTest extends AbstractRestTest {
    public void testCreation() throws Exception {
        BindingFactoryManager bfm = getBus().getExtension(BindingFactoryManager.class);
        bfm.registerBindingFactory(HttpBindingFactory.HTTP_BINDING_ID, new HttpBindingFactory());
        
        JaxWsServiceFactoryBean sf = new JaxWsServiceFactoryBean();
        sf.setBus(getBus());
        sf.setServiceClass(CustomerService.class);
        sf.setWrapped(true);
        
        Service service = sf.create();
        assertNotNull(service.getServiceInfo());
        assertEquals("http://cxf.apache.org/jra", service.getName().getNamespaceURI());
        
        HttpBindingInfoFactoryBean jraFactory = new HttpBindingInfoFactoryBean();
        
        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setServiceFactory(sf);
        svrFactory.setBus(getBus());
        svrFactory.setBindingFactory(jraFactory);
        svrFactory.setAddress("http://localhost:9001/");
        svrFactory.setTransportId("http://schemas.xmlsoap.org/wsdl/http/");
        svrFactory.setStart(false);
        
        ServerImpl svr = (ServerImpl) svrFactory.create();
        ((JettyHTTPDestination) svr.getDestination()).setContextMatchStrategy("stem");
        svr.start();
                
        URIMapper mapper = (URIMapper) service.get(URIMapper.class.getName());
        assertNotNull(mapper);
        
        BindingOperationInfo bop = mapper.getOperation("/customers", "GET", null);
        assertNotNull(bop);
        assertEquals("getCustomers", bop.getName().getLocalPart());
        assertTrue(bop.isUnwrappedCapable());
        
        bop = mapper.getOperation("/customers", "POST", null);
        assertNotNull(bop);
        assertEquals("addCustomer", bop.getName().getLocalPart());
        
        bop = mapper.getOperation("/customers/123", "GET", null);
        assertNotNull(bop);     
        assertEquals("getCustomer", bop.getName().getLocalPart());
        
        bop = mapper.getOperation("/customers/123", "PUT", null);
        assertNotNull(bop);
        assertEquals("updateCustomer", bop.getName().getLocalPart());
        
        // TEST POST/GETs
        
        Document res = get("http://localhost:9001/customers");
        assertNotNull(res);
        DOMUtils.writeXml(res, System.out);
        
        addNamespace("c", "http://cxf.apache.org/jra");
        assertValid("/c:getCustomersResponse/c:customers", res);
        assertValid("/c:getCustomersResponse/c:customers/c:customer/c:id[text()='123']", res);
        assertValid("/c:getCustomersResponse/c:customers/c:customer/c:name[text()='Dan Diephouse']", res);
        
        res = get("http://localhost:9001/customers/123");
        assertNotNull(res);
        DOMUtils.writeXml(res, System.out);
        
        addNamespace("c", "http://cxf.apache.org/jra");
        assertValid("/c:getCustomerResponse/c:customer", res);
        assertValid("/c:getCustomerResponse/c:customer/c:id[text()='123']", res);
        assertValid("/c:getCustomerResponse/c:customer/c:name[text()='Dan Diephouse']", res);
        
        res = put("http://localhost:9001/customers/123", "update.xml");
        assertNotNull(res);
        DOMUtils.writeXml(res, System.out);
        
        assertValid("/c:updateCustomerResponse", res);
        
        res = post("http://localhost:9001/customers", "add.xml");
        assertNotNull(res);
        DOMUtils.writeXml(res, System.out);
        
        assertValid("/c:addCustomerResponse", res);

        // Get the updated document
        res = get("http://localhost:9001/customers/123");
        assertNotNull(res);
        DOMUtils.writeXml(res, System.out);
        
        assertValid("/c:getCustomerResponse/c:customer", res);
        assertValid("/c:getCustomerResponse/c:customer/c:id[text()='123']", res);
        assertValid("/c:getCustomerResponse/c:customer/c:name[text()='Danno Manno']", res);

        svr.stop();
    }

}
