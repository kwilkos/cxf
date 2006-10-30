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
import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.AbstractJaxWsTest;
import org.apache.cxf.mtom_xop.HelloImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.service.model.FaultInfo;
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

        String ns = "http://apache.org/hello_world_soap_http";
        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals(ns, service.getName().getNamespaceURI());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        
        OperationInfo op = intf.getOperation(new QName(ns, "sayHi"));
        
        Class wrapper = (Class) op.getInput().getMessageParts().get(0).getTypeClass();
        assertNotNull(wrapper);
        
        wrapper = (Class) op.getOutput().getMessageParts().get(0).getTypeClass();
        assertNotNull(wrapper);
    
        assertEquals(invoker, service.getInvoker());
        
        op = intf.getOperation(new QName(ns, "testDocLitFault"));
        Collection<FaultInfo> faults = op.getFaults();
        assertEquals(2, faults.size());
        
        FaultInfo f = op.getFault(new QName(ns, "BadRecordLitFault"));
        assertNotNull(f);
        Class c = f.getProperty(Class.class.getName(), Class.class);
        assertNotNull(c);
        
        assertEquals(1, f.getMessageParts().size());
        MessagePartInfo mpi = f.getMessagePartByIndex(0);
        assertNotNull(mpi.getTypeClass());
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
        assertEquals(String.class, part.getTypeClass());
        
        assertTrue(itr.hasNext());
        part = itr.next();
        assertEquals(Boolean.TRUE, part.getProperty(JaxWsServiceFactoryBean.MODE_INOUT));
        assertEquals(byte[].class, part.getTypeClass());
        
        assertFalse(itr.hasNext());
        
        // test output setup
        itr = op.getOutput().getMessageParts().iterator();

        assertTrue(itr.hasNext());
        part = itr.next();
        assertEquals(Boolean.TRUE, part.getProperty(JaxWsServiceFactoryBean.MODE_INOUT));
        
        assertFalse(itr.hasNext());
    }
}
