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
import java.util.Map;

import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapDestinationFactory;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;

public class ReflectionServiceFactoryTest extends AbstractCXFTest {
    private ReflectionServiceFactoryBean serviceFactory;

    public void setUp() throws Exception {
        super.setUp();
        
        Bus bus = getBus();
        
        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        SoapDestinationFactory soapDF = new SoapDestinationFactory(dfm);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/", soapDF);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(LocalTransportFactory.TRANSPORT_ID, localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
    }

    public void testUnwrappedBuild() throws Exception {
        Service service = createService(false);
        
        ServiceInfo si = service.getServiceInfo();
        InterfaceInfo intf = si.getInterface();
        
        assertEquals(3, intf.getOperations().size());
        
        String ns = si.getName().getNamespaceURI();
        OperationInfo sayHelloOp = intf.getOperation(new QName(ns, "sayHello"));
        assertNotNull(sayHelloOp);
        
        assertEquals("sayHello", sayHelloOp.getInput().getName().getLocalPart());
        
        List<MessagePartInfo> messageParts = sayHelloOp.getInput().getMessageParts();
        assertEquals(0, messageParts.size());
        
        // test output
        messageParts = sayHelloOp.getOutput().getMessageParts();
        assertEquals(1, messageParts.size());
        assertEquals("sayHelloResponse", sayHelloOp.getOutput().getName().getLocalPart());
        
        MessagePartInfo mpi = messageParts.get(0);
        assertEquals("out", mpi.getName().getLocalPart());
        assertEquals(String.class, mpi.getProperty(Class.class.getName()));
    }
    
    public void testWrappedBuild() throws Exception {
        Service service = createService(true);
        
        ServiceInfo si = service.getServiceInfo();
        InterfaceInfo intf = si.getInterface();
        
        assertEquals(3, intf.getOperations().size());
        
        String ns = si.getName().getNamespaceURI();
        OperationInfo sayHelloOp = intf.getOperation(new QName(ns, "sayHello"));
        assertNotNull(sayHelloOp);
        
        assertEquals("sayHello", sayHelloOp.getInput().getName().getLocalPart());
        
        List<MessagePartInfo> messageParts = sayHelloOp.getInput().getMessageParts();
        assertEquals(1, messageParts.size());
        
        // test unwrapping
        assertTrue(sayHelloOp.isUnwrappedCapable());
        
        OperationInfo unwrappedOp = sayHelloOp.getUnwrappedOperation();
        assertEquals("sayHello", unwrappedOp.getInput().getName().getLocalPart());
        
        messageParts = unwrappedOp.getInput().getMessageParts();
        assertEquals(0, messageParts.size());
        
        // test output
        messageParts = sayHelloOp.getOutput().getMessageParts();
        assertEquals(1, messageParts.size());
        assertEquals("sayHelloResponse", sayHelloOp.getOutput().getName().getLocalPart());
        
        messageParts = unwrappedOp.getOutput().getMessageParts();
        assertEquals("sayHelloResponse", unwrappedOp.getOutput().getName().getLocalPart());
        assertEquals(1, messageParts.size());
        MessagePartInfo mpi = messageParts.get(0);
        assertEquals("out", mpi.getName().getLocalPart());
        assertEquals(String.class, mpi.getProperty(Class.class.getName()));
    }

    private Service createService(boolean wrapped) throws JAXBException {
        serviceFactory = new ReflectionServiceFactoryBean();
        serviceFactory.setDataBinding(new JAXBDataBinding(HelloService.class));
        serviceFactory.setBus(getBus());
        serviceFactory.setServiceClass(HelloService.class);
        serviceFactory.setWrapped(wrapped);
        
        return serviceFactory.create();        
    }
    
    public void testServerFactoryBean() throws Exception {
        Service service = createService(true);
        
        ServerFactoryBean svrBean = new ServerFactoryBean();
        svrBean.setAddress("http://localhost/Hello");
        svrBean.setTransportId("http://schemas.xmlsoap.org/soap/");
        svrBean.setServiceFactory(serviceFactory);
        svrBean.setBus(getBus());
        
        Server server = svrBean.create();
        assertNotNull(server);
        Map<QName, Endpoint> eps = service.getEndpoints();
        assertEquals(1, eps.size());
        
        Endpoint ep = eps.values().iterator().next();
        EndpointInfo endpointInfo = ep.getEndpointInfo();
        
        SOAPAddress soapAddress = endpointInfo.getExtensor(SOAPAddress.class);
        assertNotNull(soapAddress);
        
        BindingInfo b = service.getServiceInfo().getBindings().iterator().next();
        
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
