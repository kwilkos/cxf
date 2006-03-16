package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.FileReader;

import org.objectweb.celtix.tools.WSDLToXML;
import org.objectweb.celtix.tools.common.ToolConstants;
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
        FileReader fileReader = new FileReader(outputFile);
        char[] chars = new char[100];
        int size = 0;
        StringBuffer sb = new StringBuffer();
        while (size < outputFile.length()) {
            int readLen = fileReader.read(chars);
            sb.append(chars, 0, readLen);
            size = size + readLen;
        }
        String bindingString = new String(sb);
        int pos1 = bindingString.indexOf("<wsdl:binding name=\"Greeter_XMLBinding\" "
                                         + "type=\"tns:Greeter\">");
        int pos2 = bindingString.indexOf("<xformat:binding/>");
        int pos3 = bindingString.indexOf("<wsdl:input name=\"sayHiRequest\">");
        int pos4 = bindingString.indexOf("<xformat:body rootNode=\"tns:sayHi\" />");
        int pos5 = bindingString.indexOf("<wsdl:output name=\"sayHiResponse\">");
        int pos6 = bindingString.indexOf("<wsdl:service name=\"Greeter_XMLService\">");
        int pos7 = bindingString
            .indexOf("<wsdl:port name=\"Greeter_XMLPort\" binding=\"tns:Greeter_XMLBinding\">");
        int pos8 = bindingString.indexOf("<http:address location=\"http://localhost:9000/"
                                         + "Greeter_XMLService/Greeter_XMLPort\" />");
        assertTrue(0 < pos1 && pos1 < pos2 && pos2 < pos3 && pos3 < pos4 && pos4 < pos5);
        assertTrue(pos5 < pos6 && pos6 < pos7 && pos7 < pos8);
    }

    private String getLocation(String wsdlFile) {
        return WSDLToXMLProcessorTest.class.getResource(wsdlFile).getFile();
    }

}
