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
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.jaxws.AbstractJaxWsTest;
import org.apache.cxf.mtom_xop.HelloImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.hello_world_soap_http.GreeterImpl;

public class JaxWsServiceFactoryBeanTest extends AbstractJaxWsTest {
    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();

        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);
        Bus bus = getBus();
        bean.setBus(bus);
        bean.setServiceClass(GreeterImpl.class);

        BeanInvoker invoker = new BeanInvoker(new GreeterImpl());
        bean.setInvoker(invoker);
        
        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://apache.org/hello_world_soap_http", service.getName().getNamespaceURI());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        
        OperationInfo op = intf.getOperation(
            new QName("http://apache.org/hello_world_soap_http", "sayHi"));
        
        Class wrapper = (Class) 
            op.getUnwrappedOperation().getInput().getProperty(WrappedInInterceptor.WRAPPER_CLASS);
        assertNotNull(wrapper);
        
        wrapper = (Class) 
            op.getUnwrappedOperation().getOutput().getProperty(WrappedInInterceptor.WRAPPER_CLASS);
        assertNotNull(wrapper);
    
        assertEquals(invoker, service.getInvoker());
    }
    
    public void testHolder() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();

        Bus bus = getBus();
        bean.setBus(bus);
        bean.setServiceClass(HelloImpl.class);

        Service service = bean.create();
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        
        OperationInfo op = intf.getOperation(
            new QName("http://cxf.apache.org/mime", "echoData"));
        assertNotNull(op);
        
        // test setup of input parts
        Iterator<MessagePartInfo> itr = op.getInput().getMessageParts().iterator();
        assertTrue(itr.hasNext());
        MessagePartInfo part = itr.next();
        assertEquals("body", part.getName().getLocalPart());
        assertEquals(String.class, part.getProperty(Class.class.getName(), Class.class));
        
        assertTrue(itr.hasNext());
        part = itr.next();
        assertEquals(Boolean.TRUE, part.getProperty(JaxWsServiceFactoryBean.HOLDER));
        assertEquals(byte[].class, part.getProperty(Class.class.getName(), Class.class));
        
        assertFalse(itr.hasNext());
        
        // test output setup
        itr = op.getOutput().getMessageParts().iterator();

        assertTrue(itr.hasNext());
        part = itr.next();
        assertEquals(Boolean.TRUE, part.getProperty(JaxWsServiceFactoryBean.HOLDER));
        
        assertFalse(itr.hasNext());
    }
}
