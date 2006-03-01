package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.FileReader;

import org.objectweb.celtix.tools.WSDLToSoap;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToSoapProcessorTest
    extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }
    
    public void testDocLitWithFault() throws Exception {
        String[] args = new String[] {"-i", "Greeter", "-d",
                                      output.getCanonicalPath(),
                                      getLocation("/wsdl/hello_world_doc_lit.wsdl")};
        WSDLToSoap.main(args);

        File outputFile = new File(output, "hello_world_doc_lit-soapbinding.wsdl");
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
        String bindingString = new String(sb);
        assertTrue(bindingString.indexOf("<wsdl:binding name=\"Greeter_Binding\" " 
                                         + "type=\"tns:Greeter\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:binding style=\"document\" transport=\"http:/" 
                                         + "/schemas.xmlsoap.org/soap/http\"/>") >= 0);
        assertTrue(bindingString.indexOf("<wsdl:operation name=\"pingMe\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:operation soapAction=\"\" style=\"document\"/>") >= 0);
        assertTrue(bindingString.indexOf("<wsdl:fault name=\"pingMeFault\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:fault name=\"pingMeFault\" " 
                                         + "use=\"literal\"/>") >= 0);
        
    }

    public void testRpcLitWithoutFault() throws Exception {
        String[] args = new String[] {"-i", "GreeterRPCLit", "-n",
                                      "http://objectweb.org/hello_world_rpclit_test", "-b",
                                      "Greeter_SOAPBinding_NewBinding", "-style", "rpc", "-use",
                                      "literal", "-d", output.getCanonicalPath(), "-o",
                                      "hello_world_rpc_lit_newbinding.wsdl",
                                      getLocation("/wsdl/hello_world_rpc_lit.wsdl")};
        WSDLToSoap.main(args);

        File outputFile = new File(output, "hello_world_rpc_lit_newbinding.wsdl");
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
        String bindingString = new String(sb);
        assertTrue(bindingString
            .indexOf("<wsdl:binding name=\"Greeter_SOAPBinding_NewBinding\" " 
                     + "type=\"tns:GreeterRPCLit\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:binding style=\"rpc\" transport=\"http:/"
                                         + "/schemas.xmlsoap.org/soap/http\"/>") >= 0);
        assertTrue(bindingString.indexOf("<wsdl:operation name=\"sendReceiveData\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:operation soapAction=\"\" style=\"rpc\"/>") >= 0);
        assertTrue(bindingString.indexOf("<wsdl:input name=\"SendReceiveDataRequest\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:body use=\"literal\" namespace=\"http:/" 
                                         + "/objectweb.org/hello_world_rpclit_test\"/>") >= 0);
        assertTrue(bindingString.indexOf("<wsdl:output name=\"SendReceiveDataResponse\">") >= 0);
        assertTrue(bindingString.indexOf("<soap:body use=\"literal\" namespace=\"http:/" 
                                         + "/objectweb.org/hello_world_rpclit_test\"/>") >= 0);
    }

    public void testBindingExist() throws Exception {
        WSDLToSoapProcessor processor = new WSDLToSoapProcessor();
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_rpc_lit.wsdl"));
        env.put(ToolConstants.CFG_PORTTYPE, new String("GreeterRPCLit"));
        env.put(ToolConstants.CFG_BINDING, new String("Greeter_SOAPBinding_RPCLit"));
        processor.setEnvironment(env);
        try {
            processor.process();
            fail("Do not catch expected tool exception for binding exist!");
        } catch (Exception e) {
            if (!(e instanceof ToolException && e.toString()
                .indexOf("Input binding already exist in imported contract.") >= 0)) {
                fail("Do not catch tool exception for binding exist, "
                     + "catch other unexpected exception!");
            }
        }
    }

    public void testPortTypeNotExist() throws Exception {
        WSDLToSoapProcessor processor = new WSDLToSoapProcessor();
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_rpc_lit.wsdl"));
        env.put(ToolConstants.CFG_PORTTYPE, new String("NonExistPortType"));
        env.put(ToolConstants.CFG_BINDING, new String("NewBinding_RPCLit"));
        processor.setEnvironment(env);
        try {
            processor.process();
            fail("Do not catch expected tool exception for  binding not exist!");
        } catch (Exception e) {
            if (!(e instanceof ToolException && e.toString()
                .indexOf("Input port type does not exist in imported contract.") >= 0)) {
                fail("Do not catch tool exception for port type not exist, "
                     + "catch other unexpected exception!");
            }
        }
    }

    public void testNameSpaceMissing() throws Exception {
        WSDLToSoapProcessor processor = new WSDLToSoapProcessor();
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_rpc_lit.wsdl"));
        env.put(ToolConstants.CFG_PORTTYPE, new String("GreeterRPCLit"));
        env.put(ToolConstants.CFG_BINDING, new String("NewBinding_RPCLit"));
        env.put(ToolConstants.CFG_STYLE, new String("rpc"));
        processor.setEnvironment(env);
        try {
            processor.process();
            fail("Do not catch expected tool exception for name space missing!");
        } catch (Exception e) {
            if (!(e instanceof ToolException && e.toString()
                .indexOf("For rpc style binding, soap name space (-n) must be provided.") >= 0)) {
                fail("Do not catch tool exception for binding exist, "
                     + "catch other unexpected exception!");
            }
        }
    }

    private String getLocation(String wsdlFile) {
        return WSDLToSoapProcessorTest.class.getResource(wsdlFile).getFile();
    }

}
