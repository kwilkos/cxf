package org.objectweb.celtix.tools.processors.xsd2;

import java.io.File;
import java.io.FileReader;

import org.objectweb.celtix.tools.XSDToWSDL;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class XSDToWSDLProcessorTest
    extends ProcessorTestBase {
    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void testNewTypes() throws Exception {
        String[] args = new String[] {"-t", "http://org.objectweb/invoice", "-n", "Invoice", "-d",
                                      output.getCanonicalPath(), "-o", "Invoice_xsd.wsdl",
                                      getLocation("/wsdl/Invoice.xsd")};
        XSDToWSDL.main(args);

        File outputFile = new File(output, "Invoice_xsd.wsdl");
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
        String serviceString = new String(sb);
        assertTrue(serviceString.indexOf("<wsdl:types>") >= 0);
        assertTrue(serviceString.indexOf("<schema targetNamespace=\"http:/"
                                         + "/celtix.org/courseware/Invoice\" xmlns=\"http:/"
                                         + "/www.w3.org/2001/XMLSchema\" xmlns:soap=\"http:/"
                                         + "/schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http:/"
                                         + "/celtix.org/courseware/Invoice\" xmlns:wsdl=\"http:/"
                                         + "/schemas.xmlsoap.org/wsdl/\">") >= 0);
        assertTrue(serviceString.indexOf("<complexType name=\"InvoiceHeader\">") >= 0);
        
    }

    public void testDefaultFileName() throws Exception {
        String[] args = new String[] {"-t", "http://org.objectweb/invoice", "-n", "Invoice", "-d",
                                      output.getCanonicalPath(), getLocation("/wsdl/Invoice.xsd")};
        XSDToWSDL.main(args);

        File outputFile = new File(output, "Invoice.wsdl");
        assertTrue("PortType file is not generated", outputFile.exists());
        FileReader fileReader = new FileReader(outputFile);
        char[] chars = new char[100];
        int size = 0;
        StringBuffer sb = new StringBuffer();
        while (size < outputFile.length()) {
            int readLen = fileReader.read(chars);
            sb.append(chars, 0, readLen);
            size = size + readLen;
        }
        String serviceString = new String(sb);
        assertTrue(serviceString.indexOf("<wsdl:types>") >= 0);
        assertTrue(serviceString.indexOf("<schema targetNamespace=\"http:/"
                                         + "/celtix.org/courseware/Invoice\" xmlns=\"http:/"
                                         + "/www.w3.org/2001/XMLSchema\" xmlns:soap=\"http:/"
                                         + "/schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http:/"
                                         + "/celtix.org/courseware/Invoice\" xmlns:wsdl=\"http:/"
                                         + "/schemas.xmlsoap.org/wsdl/\">") >= 0);
        assertTrue(serviceString.indexOf("<complexType name=\"InvoiceHeader\">") >= 0);        
    }


    private String getLocation(String wsdlFile) {
        return XSDToWSDLProcessorTest.class.getResource(wsdlFile).getFile();
    }

}
