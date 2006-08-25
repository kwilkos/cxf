package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.WSDLToXML;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormat;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormatBinding;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLHttpAddress;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToXMLProcessorTest extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void testAllDefault() throws Exception {
        String[] args = new String[] {"-i", "Greeter", "-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/hello_world.wsdl")};
        WSDLToXML.main(args);

        File outputFile = new File(output, "hello_world-xmlbinding.wsdl");
        assertTrue("New wsdl file is not generated", outputFile.exists());

        WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
        processor.setEnvironment(env);
        try {
            processor.parseWSDL(outputFile.getAbsolutePath());
            Binding binding = processor.getWSDLDefinition().getBinding(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "Greeter_XMLBinding"));
            if (binding == null) {
                fail("Element wsdl:binding Greeter_XMLBinding Missed!");
            }
            Iterator it = binding.getExtensibilityElements().iterator();
            boolean found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof XMLFormatBinding) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Element <xformat:binding/> Missed!");
            }
            BindingOperation bo = binding.getBindingOperation("sayHi", null, null);
            if (bo == null) {
                fail("Element <wsdl:operation name=\"sayHi\"> Missed!");
            }
            it = bo.getBindingInput().getExtensibilityElements().iterator();
            found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof XMLFormat 
                    && ((XMLFormat)obj).getRootNode().getLocalPart().equals("sayHi")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Element <xformat:body rootNode=\"tns:sayHi\" /> Missed!");
            }
            Service service = processor.getWSDLDefinition().getService(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "Greeter_XMLService"));
            if (service == null) {
                fail("Element wsdl:service Greeter_XMLService Missed!");
            }
            it = service.getPort("Greeter_XMLPort").getExtensibilityElements().iterator();
            found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof XMLHttpAddress) {
                    XMLHttpAddress xmlHttpAddress = (XMLHttpAddress) obj;
                    if (xmlHttpAddress.getLocation() != null) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                fail("Element http:address of service port Missed!");
            }
        } catch (ToolException e) {
            fail("Exception Encountered when parsing wsdl, error: " + e.getMessage());
        }
    }

    private String getLocation(String wsdlFile) {
        return WSDLToXMLProcessorTest.class.getResource(wsdlFile).getFile();
    }

}
