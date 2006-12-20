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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.TypeInfo;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.apache.cxf.wsdl4jutils.WSDLLocatorImpl;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class WSDLServiceBuilderTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(WSDLServiceBuilderTest.class.getName());

    private static final String WSDL_PATH = "hello_world.wsdl";

    private static final String BARE_WSDL_PATH = "hello_world_bare.wsdl";

    private static final String IMPORT_WSDL_PATH = "hello_world_schema_import.wsdl";

    private Definition def;

    private Service service;

    private ServiceInfo serviceInfo;

    private IMocksControl control;

    private Bus bus;

    private BindingFactoryManager bindingFactoryManager;

    private DestinationFactoryManager destinationFactoryManager;

    public void setUp() throws Exception {
        setUpWSDL(WSDL_PATH, 0);
    }


    
    private void setUpWSDL(String wsdl, int serviceSeq) throws Exception {
        String wsdlUrl = getClass().getResource(wsdl).toString();
        LOG.info("the path of wsdl file is " + wsdlUrl);
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        
        def = wsdlReader.readWSDL(new WSDLLocatorImpl(wsdlUrl));

        WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);
        int seq = 0;
        for (Service serv : CastUtils.cast(def.getServices().values(), Service.class)) {
            if (serv != null) {
                service = serv;
                if (seq == serviceSeq) {
                    break;
                } else {
                    seq++;
                }
            }
        }

        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        destinationFactoryManager = control.createMock(DestinationFactoryManager.class);
        wsdlServiceBuilder = new WSDLServiceBuilder(bus);

        EasyMock.expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bindingFactoryManager);
        EasyMock.expect(bus.getExtension(DestinationFactoryManager.class)).andReturn(
                destinationFactoryManager);

        control.replay();
        serviceInfo = wsdlServiceBuilder.buildService(def, service);

    }

    public void tearDown() throws Exception {
        control.verify();
    }

    public void testServiceInfo() throws Exception {
        assertEquals("SOAPService", serviceInfo.getName().getLocalPart());
        assertEquals("http://apache.org/hello_world_soap_http", serviceInfo.getName().getNamespaceURI());
        assertEquals("http://apache.org/hello_world_soap_http", serviceInfo.getTargetNamespace());
        assertTrue(serviceInfo.getProperty(WSDLServiceBuilder.WSDL_DEFINITION) == def);
        assertTrue(serviceInfo.getProperty(WSDLServiceBuilder.WSDL_SERVICE) == service);

        assertEquals("Incorrect number of endpoints", serviceInfo.getEndpoints().size(), 1);
        EndpointInfo ei = serviceInfo.getEndpoint(new QName("http://apache.org/hello_world_soap_http",
                "SoapPort"));
        assertNotNull(ei);
        assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", ei.getTransportId());
        assertNotNull(ei.getBinding());
    }

    public void testInterfaceInfo() throws Exception {
        assertEquals("Greeter", serviceInfo.getInterface().getName().getLocalPart());
    }

    public void testOperationInfo() throws Exception {
        QName name = new QName(serviceInfo.getName().getNamespaceURI(), "sayHi");
        assertEquals(serviceInfo.getInterface().getOperations().size(), 4);
        OperationInfo sayHi = serviceInfo.getInterface().getOperation(
                new QName(serviceInfo.getName().getNamespaceURI(), "sayHi"));
        assertNotNull(sayHi);
        assertEquals(sayHi.getName(), name);
        assertFalse(sayHi.isOneWay());
        assertTrue(sayHi.hasInput());
        assertTrue(sayHi.hasOutput());

        name = new QName(serviceInfo.getName().getNamespaceURI(), "greetMe");
        OperationInfo greetMe = serviceInfo.getInterface().getOperation(name);
        assertNotNull(greetMe);
        assertEquals(greetMe.getName(), name);
        assertFalse(greetMe.isOneWay());
        assertTrue(greetMe.hasInput());
        assertTrue(greetMe.hasOutput());

        List<MessagePartInfo> inParts = greetMe.getInput().getMessageParts();
        assertEquals(1, inParts.size());
        MessagePartInfo part = inParts.get(0);
        assertNotNull(part.getXmlSchema());
        assertTrue(part.getXmlSchema() instanceof XmlSchemaElement);

        List<MessagePartInfo> outParts = greetMe.getOutput().getMessageParts();
        assertEquals(1, outParts.size());
        part = outParts.get(0);
        assertNotNull(part.getXmlSchema());
        assertTrue(part.getXmlSchema() instanceof XmlSchemaElement);

        assertTrue("greatMe should be wrapped", greetMe.isUnwrappedCapable());
        OperationInfo greetMeUnwrapped = greetMe.getUnwrappedOperation();

        assertNotNull(greetMeUnwrapped.getInput());
        assertNotNull(greetMeUnwrapped.getOutput());
        assertEquals("wrapped part not set", 1, greetMeUnwrapped.getInput().size());
        assertEquals("wrapped part not set", 1, greetMeUnwrapped.getOutput().size());
        assertEquals("wrapper part name wrong", "requestType", greetMeUnwrapped.getInput()
                .getMessagePartByIndex(0).getName().getLocalPart());
        assertEquals("wrapper part type name wrong", "MyStringType", greetMeUnwrapped.getInput()
                .getMessagePartByIndex(0).getTypeQName().getLocalPart());

        assertEquals("wrapper part name wrong", "responseType", greetMeUnwrapped.getOutput()
                .getMessagePartByIndex(0).getName().getLocalPart());
        assertEquals("wrapper part type name wrong", "string", greetMeUnwrapped.getOutput()
                .getMessagePartByIndex(0).getTypeQName().getLocalPart());

        name = new QName(serviceInfo.getName().getNamespaceURI(), "greetMeOneWay");
        OperationInfo greetMeOneWay = serviceInfo.getInterface().getOperation(name);
        assertNotNull(greetMeOneWay);
        assertEquals(greetMeOneWay.getName(), name);
        assertTrue(greetMeOneWay.isOneWay());
        assertTrue(greetMeOneWay.hasInput());
        assertFalse(greetMeOneWay.hasOutput());

        OperationInfo greetMeOneWayUnwrapped = greetMeOneWay.getUnwrappedOperation();
        assertNotNull(greetMeOneWayUnwrapped);
        assertNotNull(greetMeOneWayUnwrapped.getInput());
        assertNull(greetMeOneWayUnwrapped.getOutput());
        assertEquals("wrapped part not set", 1, greetMeOneWayUnwrapped.getInput().size());

        name = new QName(serviceInfo.getName().getNamespaceURI(), "pingMe");
        OperationInfo pingMe = serviceInfo.getInterface().getOperation(name);
        assertNotNull(pingMe);
        assertEquals(pingMe.getName(), name);
        assertFalse(pingMe.isOneWay());
        assertTrue(pingMe.hasInput());
        assertTrue(pingMe.hasOutput());

        assertNull(serviceInfo.getInterface().getOperation(new QName("what ever")));
    }

    public void testBindingInfo() throws Exception {
        BindingInfo bindingInfo = null;
        assertEquals(1, serviceInfo.getBindings().size());
        bindingInfo = serviceInfo.getBindings().iterator().next();
        assertNotNull(bindingInfo);
        assertEquals(bindingInfo.getInterface().getName().getLocalPart(), "Greeter");
        assertEquals(bindingInfo.getName().getLocalPart(), "Greeter_SOAPBinding");
        assertEquals(bindingInfo.getName().getNamespaceURI(), "http://apache.org/hello_world_soap_http");
    }

    public void testBindingOperationInfo() throws Exception {
        BindingInfo bindingInfo = null;
        bindingInfo = serviceInfo.getBindings().iterator().next();
        Collection<BindingOperationInfo> bindingOperationInfos = bindingInfo.getOperations();
        assertNotNull(bindingOperationInfos);
        assertEquals(bindingOperationInfos.size(), 4);
        LOG.info("the binding operation is " + bindingOperationInfos.iterator().next().getName());

        QName name = new QName(serviceInfo.getName().getNamespaceURI(), "sayHi");
        BindingOperationInfo sayHi = bindingInfo.getOperation(name);
        assertNotNull(sayHi);
        assertEquals(sayHi.getName(), name);

        name = new QName(serviceInfo.getName().getNamespaceURI(), "greetMe");
        BindingOperationInfo greetMe = bindingInfo.getOperation(name);
        assertNotNull(greetMe);
        assertEquals(greetMe.getName(), name);

        name = new QName(serviceInfo.getName().getNamespaceURI(), "greetMeOneWay");
        BindingOperationInfo greetMeOneWay = bindingInfo.getOperation(name);
        assertNotNull(greetMeOneWay);
        assertEquals(greetMeOneWay.getName(), name);

        name = new QName(serviceInfo.getName().getNamespaceURI(), "pingMe");
        BindingOperationInfo pingMe = bindingInfo.getOperation(name);
        assertNotNull(pingMe);
        assertEquals(pingMe.getName(), name);
    }

    public void testBindingMessageInfo() throws Exception {
        BindingInfo bindingInfo = null;
        bindingInfo = serviceInfo.getBindings().iterator().next();

        QName name = new QName(serviceInfo.getName().getNamespaceURI(), "sayHi");
        BindingOperationInfo sayHi = bindingInfo.getOperation(name);
        BindingMessageInfo input = sayHi.getInput();
        assertNotNull(input);
        assertEquals(input.getMessageInfo().getName().getLocalPart(), "sayHiRequest");
        assertEquals(input.getMessageInfo().getName().getNamespaceURI(),
                "http://apache.org/hello_world_soap_http");
        assertEquals(input.getMessageInfo().getMessageParts().size(), 1);
        assertEquals(input.getMessageInfo().getMessageParts().get(0).getName().getLocalPart(), "in");
        assertEquals(input.getMessageInfo().getMessageParts().get(0).getName().getNamespaceURI(),
                "http://apache.org/hello_world_soap_http");
        assertTrue(input.getMessageInfo().getMessageParts().get(0).isElement());
        QName elementName = input.getMessageInfo().getMessageParts().get(0).getElementQName();
        assertEquals(elementName.getLocalPart(), "sayHi");
        assertEquals(elementName.getNamespaceURI(), "http://apache.org/hello_world_soap_http/types");

        BindingMessageInfo output = sayHi.getOutput();
        assertNotNull(output);
        assertEquals(output.getMessageInfo().getName().getLocalPart(), "sayHiResponse");
        assertEquals(output.getMessageInfo().getName().getNamespaceURI(),
                "http://apache.org/hello_world_soap_http");
        assertEquals(output.getMessageInfo().getMessageParts().size(), 1);
        assertEquals(output.getMessageInfo().getMessageParts().get(0).getName().getLocalPart(), "out");
        assertEquals(output.getMessageInfo().getMessageParts().get(0).getName().getNamespaceURI(),
                "http://apache.org/hello_world_soap_http");
        assertTrue(output.getMessageInfo().getMessageParts().get(0).isElement());
        elementName = output.getMessageInfo().getMessageParts().get(0).getElementQName();
        assertEquals(elementName.getLocalPart(), "sayHiResponse");
        assertEquals(elementName.getNamespaceURI(), "http://apache.org/hello_world_soap_http/types");

        assertTrue(sayHi.getFaults().size() == 0);

        name = new QName(serviceInfo.getName().getNamespaceURI(), "pingMe");
        BindingOperationInfo pingMe = bindingInfo.getOperation(name);
        assertNotNull(pingMe);
        assertEquals(1, pingMe.getFaults().size());
        BindingFaultInfo fault = pingMe.getFaults().iterator().next();

        assertNotNull(fault);
        assertEquals(fault.getFaultInfo().getName().getLocalPart(), "pingMeFault");
        assertEquals(fault.getFaultInfo().getName().getNamespaceURI(),
                "http://apache.org/hello_world_soap_http");
        assertEquals(fault.getFaultInfo().getMessageParts().size(), 1);
        assertEquals(fault.getFaultInfo().getMessageParts().get(0).getName().getLocalPart(), "faultDetail");
        assertEquals(fault.getFaultInfo().getMessageParts().get(0).getName().getNamespaceURI(),
                "http://apache.org/hello_world_soap_http");
        assertTrue(fault.getFaultInfo().getMessageParts().get(0).isElement());
        elementName = fault.getFaultInfo().getMessageParts().get(0).getElementQName();
        assertEquals(elementName.getLocalPart(), "faultDetail");
        assertEquals(elementName.getNamespaceURI(), "http://apache.org/hello_world_soap_http/types");
    }

    public void testSchema() {
        XmlSchemaCollection schemas = serviceInfo.getProperty(WSDLServiceBuilder.WSDL_SCHEMA_LIST,
                XmlSchemaCollection.class);
        assertNotNull(schemas);
        TypeInfo typeInfo = serviceInfo.getTypeInfo();
        assertNotNull(typeInfo);
        assertEquals(typeInfo.getSchemas().size(), 1);
        SchemaInfo schemaInfo = typeInfo.getSchemas().iterator().next();
        assertNotNull(schemaInfo);
        assertEquals(schemaInfo.getNamespaceURI(), "http://apache.org/hello_world_soap_http/types");
        assertEquals(schemas.read(schemaInfo.getElement()).getTargetNamespace(),
                "http://apache.org/hello_world_soap_http/types");
        // add below code to test the creation of javax.xml.validation.Schema
        // with schema in serviceInfo
        Schema schema = EndpointReferenceUtils.getSchema(serviceInfo);
        assertNotNull(schema);
    }

    public void testBare() throws Exception {
        setUpWSDL(BARE_WSDL_PATH, 0);
        BindingInfo bindingInfo = null;
        bindingInfo = serviceInfo.getBindings().iterator().next();
        Collection<BindingOperationInfo> bindingOperationInfos = bindingInfo.getOperations();
        assertNotNull(bindingOperationInfos);
        assertEquals(bindingOperationInfos.size(), 1);
        LOG.info("the binding operation is " + bindingOperationInfos.iterator().next().getName());
        QName name = new QName(serviceInfo.getName().getNamespaceURI(), "greetMe");
        BindingOperationInfo greetMe = bindingInfo.getOperation(name);
        assertNotNull(greetMe);
        assertEquals("greetMe OperationInfo name error", greetMe.getName(), name);
        assertFalse("greetMe should be a Unwrapped operation ", greetMe.isUnwrappedCapable());
    }

    public void testImport() throws Exception {
        // rewrite the schema1.xsd to import schema2.xsd with absolute path.
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(this.getClass().getResourceAsStream("./s1/s2/schema2.xsd"));
        Element schemaImport = null;
        for (int i = 0; i < doc.getChildNodes().getLength(); i++) {
            if (doc.getChildNodes().item(i) instanceof Element) {
                Element schema = (Element) doc.getChildNodes().item(i);
                for (int j = 0; j < schema.getChildNodes().getLength(); j++) {
                    if (schema.getChildNodes().item(j) instanceof Element) {
                        schemaImport = (Element) schema.getChildNodes().item(j);
                        break;
                    }
                }
                break;
            }
        }
        if (schemaImport == null) {
            fail("Can't find import element");
        }
        String filePath = this.getClass().getResource("./s1/s2/s4/schema4.xsd").getFile();
        String importPath = schemaImport.getAttributeNode("schemaLocation").getValue();
        if (!new URI(URLEncoder.encode(importPath, "utf-8")).isAbsolute()) {
            schemaImport.getAttributeNode("schemaLocation").setNodeValue("file:" + filePath);            
            String fileStr = this.getClass().getResource("./s1/s2/schema2.xsd").getFile();
            fileStr = URLDecoder.decode(fileStr, "utf-8");
            File file = new File(fileStr);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fout = new FileOutputStream(file);
            XMLUtils.writeTo(doc, fout);
            fout.flush();
            fout.close();
        }
        setUpWSDL(IMPORT_WSDL_PATH, 0);
        TypeInfo types = serviceInfo.getTypeInfo();
        assertNotNull(types);
        assertNotNull(types.getSchemas());
        Element ele = types.getSchemas().iterator().next().getElement();
        assertNotNull(ele);
        Schema schema = EndpointReferenceUtils.getSchema(serviceInfo);        
        assertNotNull(schema);        
    }

    
    public void testDiffPortTypeNsImport() throws Exception {
        setUpWSDL("/DiffPortTypeNs.wsdl", 0);        
        doDiffPortTypeNsImport();
        setUpWSDL("/DiffPortTypeNs.wsdl", 1);
        doDiffPortTypeNsImport();
    }
    
    private void doDiffPortTypeNsImport() {
        if (serviceInfo.getName().getLocalPart().endsWith("Rpc")) {
            String ns = serviceInfo.getInterface().getName().getNamespaceURI();
            OperationInfo oi = serviceInfo.getInterface().getOperation(new QName(ns, "NewOperationRpc"));
            assertNotNull(oi);
            ns = oi.getInput().getName().getNamespaceURI();
            MessagePartInfo mpi = oi.getInput().getMessagePart(new QName(ns, "NewOperationRequestRpc"));
            assertNotNull(mpi);                    
        } else {
            String ns = serviceInfo.getInterface().getName().getNamespaceURI();
            OperationInfo oi = serviceInfo.getInterface().getOperation(new QName(ns, "NewOperation"));
            assertNotNull(oi);
            ns = oi.getInput().getName().getNamespaceURI();
            MessagePartInfo mpi = oi.getInput().getMessagePart(new QName(ns, "NewOperationRequest"));
            assertNotNull(mpi);                    
        }
    }
}
