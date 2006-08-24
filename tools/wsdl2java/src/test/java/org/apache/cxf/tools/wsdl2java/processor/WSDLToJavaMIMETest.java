package org.apache.cxf.tools.wsdl2java.processor;

//import java.io.File;
//import java.io.FileReader;
////import java.io.FileReader;
//
//import org.apache.cxf.tools.WSDLToJava;
//import org.apache.cxf.tools.common.toolspec.ToolException;
import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;

public class WSDLToJavaMIMETest
    extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

//    public void testHelloWorld() throws Exception {
//        String[] args = new String[] {"-d", output.getCanonicalPath(),
//                                      getLocation("/wsdl/binary_attachment.wsdl")};
//        WSDLToJava.main(args);
//        assertNotNull(output);
//        File org = new File(output, "org");
//        assertTrue(org.exists());
//        File apache = new File(org, "apache");
//        assertTrue(apache.exists());
//        File helloworldsoaphttp = new File(apache, "binary_attachment");
//        assertTrue(helloworldsoaphttp.exists());
//        File outputFile = new File(helloworldsoaphttp, "BinaryAttachmentPortType.java");
//        assertTrue("PortType file is not generated", outputFile.exists());
//        FileReader fileReader = new FileReader(outputFile);
//        char[] chars = new char[100];
//        int size = 0;
//        StringBuffer sb = new StringBuffer();
//        while (size < outputFile.length()) {
//            int readLen = fileReader.read(chars);
//            sb.append(chars, 0, readLen);
//            size = size + readLen;
//        }
//        String serviceString = new String(sb);
//        int position1 = serviceString.indexOf("public byte[] echoImage(");
//        int position2 = serviceString.indexOf("byte[] para0");
//        int position3 = serviceString.indexOf("java.awt.Image para1,");
//        int position4 = serviceString
//            .indexOf("javax.xml.ws.Holder<javax.activation.DataHandler> retn1");
//        assertTrue(position1 > 0 && position2 > 0 && position3 > 0 && position4 > 0);
//        assertTrue(position1 < position2 && position2 < position3 && position3 < position4);
//    }
//
//    public void testWithExternalBindingSwitch() throws Exception {
//        String[] args = new String[] {"-d", output.getCanonicalPath(), "-b",
//                                      getLocation("/wsdl/mime_binding.wsdl"),
//                                      getLocation("/wsdl/binary_attachment.wsdl")};
//        WSDLToJava.main(args);
//        assertNotNull(output);
//        File org = new File(output, "org");
//        assertTrue(org.exists());
//        File apache = new File(org, "apache");
//        assertTrue(apache.exists());
//        File helloworldsoaphttp = new File(apache, "binary_attachment");
//        assertTrue(helloworldsoaphttp.exists());
//        File outputFile = new File(helloworldsoaphttp, "BinaryAttachmentPortType.java");
//        assertTrue("PortType file is not generated", outputFile.exists());
//        FileReader fileReader = new FileReader(outputFile);
//        char[] chars = new char[100];
//        int size = 0;
//        StringBuffer sb = new StringBuffer();
//        while (size < outputFile.length()) {
//            int readLen = fileReader.read(chars);
//            sb.append(chars, 0, readLen);
//            size = size + readLen;
//        }
//        String serviceString = new String(sb);
//        int position1 = serviceString.indexOf("public java.awt.Image echoImage(");
//        int position2 = serviceString.indexOf("java.awt.Image para0");
//        int position3 = serviceString.indexOf("public void echoMultipleImage(");
//        int position4 = serviceString.indexOf("java.awt.Image para1,");
//        int position5 = serviceString
//            .indexOf("javax.xml.ws.Holder<javax.activation.DataHandler> retn1");
////        System.out.println("position1=" + position1 + "; position2=" + position2 + "; position3="
////                           + position3 + "; position4=" + position4 + "; position5=" + position5);
//        assertTrue(position1 > 0 && position2 > 0 && position3 > 0 && position4 > 0
//                   && position5 > 0);
//        assertTrue(position1 < position2 && position2 < position3 && position3 < position4
//                   && position4 < position5);
//    }
//
//    public void testMIMEValidationUniqueRoot() throws Exception {
//        WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
//        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
//        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/mime_fail_unique_root.wsdl"));
//        processor.setEnvironment(env);
//        try {
//            processor.process();
//            fail("Do not catch expected tool exception for MIME unique root validation failure!");
//        } catch (Exception e) {
//            if (!(e instanceof ToolException && e.toString()
//                .indexOf("There's more than one soap body mime part in its binding input") >= 0)) {
//                fail("Do not catch expected tool exception for MIME unique root validation failure,"
//                     + " catch other unexpected exception!");
//            }
//        }
//    }
//
//    public void testMIMEValidationDiffParts() throws Exception {
//        WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
//        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
//        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/mime_fail_diff_parts.wsdl"));
//        processor.setEnvironment(env);
//        try {
//            processor.process();
//            fail("Do not catch expected tool exception for MIME different parts validation failure!");
//        } catch (Exception e) {
//            if (!(e instanceof ToolException && e.toString()
//                .indexOf("Part attribute value for meme:content elements are different") >= 0)) {
//                fail("Do not catch expected tool exception for MIME different parts validation failure,"
//                     + " catch other unexpected exception!");
//            }
//        }
//    }

//    private String getLocation(String wsdlFile) {
//        return WSDLToJavaMIMETest.class.getResource(wsdlFile).getFile();
//    }
    
    public void testDummy() {
        System.out.print("");
    }
    
}
