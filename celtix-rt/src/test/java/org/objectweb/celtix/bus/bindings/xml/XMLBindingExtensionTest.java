package org.objectweb.celtix.bus.bindings.xml;

import java.net.*;
import java.util.*;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;

public class XMLBindingExtensionTest extends TestCase {

    public void testExtensionRegister() throws Exception {
        Bus bus = Bus.init();
        int inCount = 0;
        int outCount = 0;
        ExtensionRegistry registry = bus.getWSDLManager().getExtenstionRegistry();
        assertNotNull(registry);
        
        if (registry.getAllowableExtensions(BindingInput.class) != null) {
            inCount = registry.getAllowableExtensions(BindingInput.class).size();
        }
        
        if (registry.getAllowableExtensions(BindingOutput.class) != null) {
            outCount = registry.getAllowableExtensions(BindingOutput.class).size();
        }

        Set inputSet = registry.getAllowableExtensions(BindingInput.class);
        Set outputSet = registry.getAllowableExtensions(BindingOutput.class);

        assertNotNull(inputSet);
        assertNotNull(outputSet);
        
        assertEquals(inputSet.size() - inCount, outputSet.size() - outCount);
        // Since during the bus init, the xml binding factory already register the extensor
        // So, there should have no difference.
        assertTrue(inputSet.size() == inCount);
        assertTrue(outputSet.size() == outCount);
    }

    public void testXMLBindingExtensor() throws Exception {
        Bus bus = Bus.init();
        ExtensionRegistry registry = bus.getWSDLManager().getExtenstionRegistry();
        assertNotNull(registry);
        
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world_xml_bare.wsdl");
        Definition definition = bus.getWSDLManager().getDefinition(wsdlUrl);
        assertNotNull(definition);
        QName wsdlName = new QName("http://objectweb.org/hello_world_xml_http/bare", "HelloWorld");
        assertEquals(definition.getQName(), wsdlName);

        QName bindingName = new QName("http://objectweb.org/hello_world_xml_http/bare", "Greeter_XMLBinding");
        Binding binding = definition.getBinding(bindingName);
        assertNotNull(binding);

        BindingOperation operation = binding.getBindingOperation("sayHi", "sayHiRequest", "sayHiResponse");
        assertNotNull(operation);
        BindingInput input = operation.getBindingInput();
        assertNotNull(input);

        XMLBinding xmlBinding = null;
        Iterator ite = input.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof XMLBinding) {
                xmlBinding = (XMLBinding)obj;
            }
        }
        assertNotNull(xmlBinding);
        assertEquals(new QName("http://objectweb.org/hello_world_xml_http/bare", "sayHi"),
                     xmlBinding.getRootNode());
    }
}
