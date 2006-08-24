package org.apache.cxf.tools.wsdl2java.processor;

import java.io.File;
import java.io.FileReader;

import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;

public class WSDLTOJavaEOLStyleTest extends ProcessorTestBase {

    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
    }

    public void testHelloWorld() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        processor.setEnvironment(env);
        processor.process();
        File seiFile = new File(output.getCanonicalPath()
                                + "/org/objectweb/hello_world_soap_http/Greeter.java");
        assertTrue("PortType file is not generated", seiFile.exists());
        FileReader fileReader = new FileReader(seiFile);
        char[] chars = new char[100];
        int size = 0;
        StringBuffer sb = new StringBuffer();
        while (size < seiFile.length()) {
            int readLen = fileReader.read(chars);
            sb.append(chars, 0, readLen);
            size = size + readLen;

        }
        String seiString = new String(sb);
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            assertTrue("EOL Style is not correct on windows platform", seiString.indexOf("\r\n") >= 0);
        } else {
            assertTrue("EOL Style is not correct on unix platform", seiString.indexOf("\r") < 0);
        }

    }

    private String getLocation(String wsdlFile) {
        return WSDLTOJavaEOLStyleTest.class.getResource(wsdlFile).getFile();
    }
}
