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

package org.apache.cxf.wsdl11;

import java.util.Collection;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceWSDLBuilderTest extends Assert {

    private static final Logger LOG = Logger.getLogger(ServiceWSDLBuilderTest.class.getName());
    private static final String WSDL_PATH = "hello_world.wsdl";
    
    private Definition def;
    private Definition newDef;
    private Service service;

    private WSDLServiceBuilder wsdlServiceBuilder;
    private ServiceInfo serviceInfo;
    
    private IMocksControl control;
    private Bus bus;
    private BindingFactoryManager bindingFactoryManager;
    private DestinationFactoryManager destinationFactoryManager;
    private DestinationFactory destinationFactory;
    
    @Before
    public void setUp() throws Exception {
  
        String wsdlUrl = getClass().getResource(WSDL_PATH).toString();
        LOG.info("the path of wsdl file is " + wsdlUrl);
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        def = wsdlReader.readWSDL(wsdlUrl);
        
        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        destinationFactoryManager = control.createMock(DestinationFactoryManager.class);
        destinationFactory = control.createMock(DestinationFactory.class);
        wsdlServiceBuilder = new WSDLServiceBuilder(bus);

        for (Service serv : CastUtils.cast(def.getServices().values(), Service.class)) {
            if (serv != null) {
                service = serv;
                break;
            }
        }
        
        EasyMock.expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bindingFactoryManager);
        EasyMock.expect(bus.getExtension(DestinationFactoryManager.class))
            .andReturn(destinationFactoryManager);
        
        EasyMock.expect(destinationFactoryManager
                        .getDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/"))
            .andReturn(destinationFactory);

        control.replay();
        
        serviceInfo = wsdlServiceBuilder.buildServices(def, service).get(0);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_DEFINITION, null);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_SERVICE, null);
        newDef = new ServiceWSDLBuilder(serviceInfo).build();
        
    }
    
    @After
    public void tearDown() throws Exception {        
        control.verify();
        newDef = null;
    }
    
    @Test    
    public void testDefinition() throws Exception {
        assertEquals("http://apache.org/hello_world_soap_http", newDef.getTargetNamespace());
        Service serv = newDef.getService(new QName("http://apache.org/hello_world_soap_http",
                                                   "SOAPService"));
        assertNotNull(serv);
        assertNotNull(serv.getPort("SoapPort"));
    }
    
    @Test
    public void testPortType() throws Exception {
        assertEquals(newDef.getPortTypes().size(), 1);
        PortType portType = (PortType)newDef.getPortTypes().values().iterator().next();
        assertNotNull(portType);
        assertTrue(portType.getQName().equals(new QName(newDef.getTargetNamespace(), "Greeter")));
        
    }
    
    @Test
    public void testSayHiOperation() throws Exception {
        PortType portType = newDef.getPortType(new QName(newDef.getTargetNamespace(), 
            "Greeter"));
        Collection<Operation> operations =  
            CastUtils.cast(
                portType.getOperations(), Operation.class);
        
        assertEquals(operations.size(), 4);
        Operation sayHi = portType.getOperation("sayHi", "sayHiRequest", "sayHiResponse");
        assertNotNull(sayHi);
        assertEquals("sayHi", sayHi.getName());
        Input input = sayHi.getInput();
        assertNotNull(input);
        assertEquals(input.getName(), "sayHiRequest");
        Message message = input.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "sayHiRequest");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("in").getName(), "in");
        Output output = sayHi.getOutput();
        assertNotNull(output);
        assertEquals(output.getName(), "sayHiResponse");
        message = output.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "sayHiResponse");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("out").getName(), "out");
        assertTrue(sayHi.getFaults().size() == 0);
              
    }
    
    @Test
    public void testGreetMeOperation() throws Exception {
        PortType portType = newDef.getPortType(new QName(newDef.getTargetNamespace(), 
            "Greeter"));
        Operation greetMe = portType.getOperation("greetMe", "greetMeRequest", "greetMeResponse");
        assertNotNull(greetMe);
        assertEquals("greetMe", greetMe.getName());
        Input input = greetMe.getInput();
        assertNotNull(input);
        assertEquals(input.getName(), "greetMeRequest");
        Message message = input.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "greetMeRequest");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("in").getName(), "in");
        Output output = greetMe.getOutput();
        assertNotNull(output);
        assertEquals(output.getName(), "greetMeResponse");
        message = output.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "greetMeResponse");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("out").getName(), "out");
        assertTrue(greetMe.getFaults().size() == 0);
        
    }
    
    @Test
    public void testGreetMeOneWayOperation() throws Exception {
        PortType portType = newDef.getPortType(new QName(newDef.getTargetNamespace(), 
            "Greeter"));
        Operation greetMeOneWay = portType.getOperation("greetMeOneWay", "greetMeOneWayRequest", null);
        assertNotNull(greetMeOneWay);
        assertEquals("greetMeOneWay", greetMeOneWay.getName());
        Input input = greetMeOneWay.getInput();
        assertNotNull(input);
        assertEquals(input.getName(), "greetMeOneWayRequest");
        Message message = input.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "greetMeOneWayRequest");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("in").getName(), "in");
        Output output = greetMeOneWay.getOutput();
        assertNull(output);
        assertTrue(greetMeOneWay.getFaults().size() == 0);
    }
    
    @Test
    public void testPingMeOperation() throws Exception {
        PortType portType = newDef.getPortType(new QName(newDef.getTargetNamespace(), 
            "Greeter"));
        Operation pingMe = portType.getOperation("pingMe", "pingMeRequest", "pingMeResponse");
        assertNotNull(pingMe);
        assertEquals("pingMe", pingMe.getName());
        Input input = pingMe.getInput();
        assertNotNull(input);
        assertEquals(input.getName(), "pingMeRequest");
        Message message = input.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "pingMeRequest");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("in").getName(), "in");
        Output output = pingMe.getOutput();
        assertNotNull(output);
        assertEquals(output.getName(), "pingMeResponse");
        message = output.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "pingMeResponse");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("out").getName(), "out");
        assertTrue(pingMe.getFaults().size() == 1);
        Fault fault = pingMe.getFault("pingMeFault");
        assertNotNull(fault);
        assertEquals(fault.getName(), "pingMeFault");
        message = fault.getMessage();
        assertNotNull(message);
        assertEquals(message.getQName().getLocalPart(), "pingMeFault");
        assertEquals(message.getQName().getNamespaceURI(), newDef.getTargetNamespace());
        assertEquals(message.getParts().size(), 1);
        assertEquals(message.getPart("faultDetail").getName(), "faultDetail");
        assertNull(message.getPart("faultDetail").getTypeName());
    }
    
    @Test
    public void testBinding() throws Exception {
        assertEquals(newDef.getBindings().size(), 1);
        Binding binding = newDef.getBinding(new QName(newDef.getTargetNamespace(), "Greeter_SOAPBinding"));
        assertNotNull(binding);
        assertEquals(binding.getBindingOperations().size(), 4);
    }
    
    @Test
    public void testSchemas() throws Exception {
        Types types = newDef.getTypes();
        assertNotNull(types);
        Collection<ExtensibilityElement> schemas = 
            CastUtils.cast(types.getExtensibilityElements(), ExtensibilityElement.class);
        assertEquals(schemas.size(), 1);
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        Element schemaElem = ((Schema)schemas.iterator().next()).getElement();
        assertEquals(schemaCollection.read(schemaElem).getTargetNamespace(), 
                     "http://apache.org/hello_world_soap_http/types");
    }
    
}
