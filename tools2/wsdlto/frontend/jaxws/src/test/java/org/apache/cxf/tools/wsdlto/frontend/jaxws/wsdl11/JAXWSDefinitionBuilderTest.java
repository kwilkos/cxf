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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.wsdl11;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion.JAXWSBinding;
import org.apache.cxf.transport.jms.AddressType;

public class JAXWSDefinitionBuilderTest extends TestCase {
    private ToolContext env;

    public void setUp() {
        env = new ToolContext();
    }

    public void testBuildDefinitionWithXMLBinding() {
        String qname = "http://apache.org/hello_world_xml_http/bare";
        String wsdlUrl = getClass().getResource("hello_world_xml_bare.wsdl").toString();

        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        Definition def = builder.build(wsdlUrl);
        assertNotNull(def);
        
        Map services = def.getServices();
        assertNotNull(services);
        assertEquals(1, services.size());
        Service service = (Service)services.get(new QName(qname, "XMLService"));
        assertNotNull(service);
        
        Map ports = service.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.size());
        Port port = service.getPort("XMLPort");
        assertNotNull(port);

        assertEquals(1, port.getExtensibilityElements().size());
        assertTrue(port.getExtensibilityElements().get(0) instanceof HTTPAddress);

        Binding binding = port.getBinding();
        assertNotNull(binding);
        assertEquals(new QName(qname, "Greeter_XMLBinding"), binding.getQName());

        BindingOperation operation = binding.getBindingOperation("sayHi", null, null);
        assertNotNull(operation);

        BindingInput input = operation.getBindingInput();
        assertNotNull(input);
        assertEquals(1, input.getExtensibilityElements().size());
        assertTrue(input.getExtensibilityElements().get(0) instanceof XMLBindingMessageFormat);
    }

    public void testBuildDefinitionWithJMSTransport() {
        String qname = "http://cxf.apache.org/hello_world_jms";
        String wsdlUrl = getClass().getResource("jms_test.wsdl").toString();

        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        Definition def = builder.build(wsdlUrl);
        assertNotNull(def);
        
        Map services = def.getServices();
        assertNotNull(services);
        assertEquals(8, services.size());
        Service service = (Service)services.get(new QName(qname, "HelloWorldQueueBinMsgService"));
        assertNotNull(service);
        
        Map ports = service.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.size());
        Port port = service.getPort("HelloWorldQueueBinMsgPort");
        assertNotNull(port);

        assertEquals(3, port.getExtensibilityElements().size());
        assertTrue(port.getExtensibilityElements().get(0) instanceof AddressType);
    }

    public void testCustomization() {
        env.put(ToolConstants.CFG_WSDLURL, getClass().getResource("./hello_world.wsdl").toString());
        env.put(ToolConstants.CFG_BINDING, getClass().getResource("./binding2.xml").toString());
        JAXWSDefinitionBuilder builder = new JAXWSDefinitionBuilder();
        builder.setContext(env);
        builder.build();
        builder.customize();

        Definition customizedDef = builder.getWSDLModel();
        List defExtensionList = customizedDef.getExtensibilityElements();
        Iterator ite = defExtensionList.iterator();

        while (ite.hasNext()) {
            ExtensibilityElement extElement = (ExtensibilityElement)ite.next();
            JAXWSBinding binding = (JAXWSBinding)extElement;
            assertEquals("Customized package name does not been parsered", "com.foo", binding.getPackage());
            assertEquals("Customized enableAsync does not parsered", true, binding.isEnableAsyncMapping());
        }

        PortType portType = customizedDef.getPortType(new QName("http://apache.org/hello_world_soap_http",
                                                                "Greeter"));

        List portTypeList = portType.getExtensibilityElements();
        JAXWSBinding binding = (JAXWSBinding)portTypeList.get(0);

        assertEquals("Customized enable EnableWrapperStyle name does not been parsered", true, binding
            .isEnableWrapperStyle());
       
        List opList = portType.getOperations();
        Operation operation = (Operation)opList.get(0);
        List extList = operation.getExtensibilityElements();
        binding = (JAXWSBinding)extList.get(0);

        assertEquals("Customized method name does not parsered", "echoMeOneWay", binding.getMethodName());
        
        
        assertEquals("Customized parameter element name does not parsered", "tns:number1", binding
            .getJaxwsPara().getElementName());
        assertEquals("Customized parameter message name does not parsered", "greetMeOneWayRequest", binding
            .getJaxwsPara().getMessageName());
        assertEquals("customized parameter name does not parsered", "num1", binding.getJaxwsPara().getName());
    }
}
