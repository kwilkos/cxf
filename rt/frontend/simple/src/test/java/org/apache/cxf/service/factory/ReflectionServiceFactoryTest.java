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
package org.apache.cxf.service.factory;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.AbstractCXFTest;

public class ReflectionServiceFactoryTest extends AbstractCXFTest {
    public void testReflectionBuilding() throws Exception {
        ReflectionServiceFactoryBean sf = new ReflectionServiceFactoryBean();
        sf.setDataBinding(new JAXBDataBinding(HelloService.class));
        sf.setBus(getBus());
        sf.setServiceClass(HelloService.class);
        
        Service service = sf.create();
        
        ServiceInfo si = service.getServiceInfo();
        InterfaceInfo intf = si.getInterface();
        
        assertEquals(3, intf.getOperations().size());
        
        String ns = si.getName().getNamespaceURI();
        OperationInfo sayHelloOp = intf.getOperation(new QName(ns, "sayHello"));
        assertNotNull(sayHelloOp);
        
        List<MessagePartInfo> messageParts = sayHelloOp.getInput().getMessageParts();
        assertEquals(0, messageParts.size());
        
        messageParts = sayHelloOp.getOutput().getMessageParts();
        assertEquals(1, messageParts.size());
        MessagePartInfo mpi = messageParts.get(0);
        assertEquals("out", mpi.getName().getLocalPart());
        assertEquals(String.class, mpi.getProperty(Class.class.getName()));
        
        BindingInfo b = si.getBindings().iterator().next();
        
        assertTrue(b instanceof SoapBindingInfo);
        
        SoapBindingInfo sb = (SoapBindingInfo) b;
        assertEquals("HelloServiceSoapBinding", b.getName().getLocalPart());
        assertEquals("document", sb.getStyle());
        
        assertEquals(3, b.getOperations().size());
        
        BindingOperationInfo bop = b.getOperations().iterator().next();
        SoapOperationInfo sop = bop.getExtensor(SoapOperationInfo.class);
        assertNotNull(sop);
        assertEquals("", sop.getAction());
        assertEquals("document", sop.getStyle());
    }
}
