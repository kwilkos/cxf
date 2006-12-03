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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.http.AbstractRestTest;
import org.apache.cxf.binding.http.HttpBindingFactory;
import org.apache.cxf.binding.http.HttpBindingInfoFactoryBean;
import org.apache.cxf.binding.http.URIMapper;
import org.apache.cxf.binding.http.strategy.ConventionStrategy;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.model.BindingOperationInfo;

public class WrappedServiceTest extends AbstractRestTest {
    
    public void testConvention() throws Exception {
        HttpBindingInfoFactoryBean hbif = new HttpBindingInfoFactoryBean();
        hbif.getStrategies().clear();
        hbif.getStrategies().add(new ConventionStrategy());
        
        testService(hbif);
    }
    
    public void testJRA() throws Exception {
        testService(new HttpBindingInfoFactoryBean());
    }

    public void testService(HttpBindingInfoFactoryBean httpFactory) throws Exception {
        BindingFactoryManager bfm = getBus().getExtension(BindingFactoryManager.class);
        bfm.registerBindingFactory(HttpBindingFactory.HTTP_BINDING_ID, new HttpBindingFactory());
        
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setBus(getBus());
        sf.setServiceClass(CustomerService.class);
        sf.getServiceFactory().setWrapped(true);
        sf.setBindingFactory(httpFactory);
        sf.setAddress("http://localhost:9001/");

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("contextMatchStrategy", "stem");
        sf.setProperties(props);
        
        ServerImpl svr = (ServerImpl) sf.create();
                
        URIMapper mapper = (URIMapper) svr.getEndpoint().getService().get(URIMapper.class.getName());
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
        
        addNamespace("c", "http://cxf.apache.org/jra");
        assertValid("/c:getCustomersResponse/c:customers", res);
        assertValid("/c:getCustomersResponse/c:customers/c:customer/c:id[text()='123']", res);
        assertValid("/c:getCustomersResponse/c:customers/c:customer/c:name[text()='Dan Diephouse']", res);
        
        res = get("http://localhost:9001/customers/123");
        assertNotNull(res);

        addNamespace("c", "http://cxf.apache.org/jra");
        assertValid("/c:getCustomerResponse/c:customer", res);
        assertValid("/c:getCustomerResponse/c:customer/c:id[text()='123']", res);
        assertValid("/c:getCustomerResponse/c:customer/c:name[text()='Dan Diephouse']", res);
        
        res = put("http://localhost:9001/customers/123", "update.xml");
        assertNotNull(res);

        assertValid("/c:updateCustomerResponse", res);
        
        res = post("http://localhost:9001/customers", "add.xml");
        assertNotNull(res);

        assertValid("/c:addCustomerResponse", res);

        // Get the updated document
        res = get("http://localhost:9001/customers/123");
        assertNotNull(res);

        assertValid("/c:getCustomerResponse/c:customer", res);
        assertValid("/c:getCustomerResponse/c:customer/c:id[text()='123']", res);
        assertValid("/c:getCustomerResponse/c:customer/c:name[text()='Danno Manno']", res);

        svr.stop();
    }

}
