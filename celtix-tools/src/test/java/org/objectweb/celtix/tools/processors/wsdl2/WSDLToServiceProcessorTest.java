package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.*;

import org.objectweb.celtix.tools.WSDLToService;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToServiceProcessorTest extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void testNewService() throws Exception {
        String[] args = new String[] {"-transport", "http", "-e", "serviceins", "-p", "portins",
                                      "-n", "Greeter_SOAPBinding", "-a",
                                      "http://localhost:9000/newservice/newport", "-d",
                                      output.getCanonicalPath(),
                                      getLocation("/wsdl/hello_world.wsdl")};
        WSDLToService.main(args);

        File outputFile = new File(output, "hello_world-service.wsdl");
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
        assertTrue(serviceString.indexOf("<wsdl:service name=\"serviceins\">") >= 0);
        assertTrue(serviceString
            .indexOf("<wsdl:port name=\"portins\" binding=\"tns:Greeter_SOAPBinding\">") >= 0);
        assertTrue(serviceString
            .indexOf("<soap:address location=\"http://localhost:9000/newservice/newport\"/>") >= 0);
    }

    public void testDefaultLocation() throws Exception {

        String[] args = new String[] {"-transport", "http", "-e", "serviceins", "-p", "portins",
                                      "-n", "Greeter_SOAPBinding", "-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/hello_world.wsdl")};
        WSDLToService.main(args);

        File outputFile = new File(output, "hello_world-service.wsdl");
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
        assertTrue(serviceString
            .indexOf("<soap:address location=\"http://localhost:9000/serviceins/portins\"/>") >= 0);
    }

    public void testJMSNewService() throws Exception {
        String[] args = new String[] {"-transport", "jms", "-e", "serviceins", "-p", "portins",
                                      "-n", "HelloWorldPortBinding", 
                                      "-jpu", "tcp://localhost:61616", 
                                      "-jcf", "org.activemq.jndi.ActiveMQInitialContextFactory", 
                                      "-jfn", "ConnectionFactory", 
                                      "-jdn", "dynamicQueues/test.celtix.jmstransport.queue", 
                                      "-jmt", "text", 
                                      "-jmc", "false", 
                                      "-jsn", "Celtix_Queue_subscriber", 
                                      "-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/jms_test.wsdl")};
        WSDLToService.main(args);
        File outputFile = new File(output, "jms_test-service.wsdl");
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
        assertTrue(serviceString.indexOf("<jms:address " 
                        + "destinationStyle=\"queue\" " 
                        + "initialContextFactory=\"org.activemq.jndi.ActiveMQInitialContextFactory\" "  
                        + "jndiConnectionFactoryName=\"ConnectionFactory\" "  
                        + "messageType=\"text\" " 
                        + "jndiProviderURL=\"tcp://localhost:61616\" "  
                        + "jndiDestinationName=\"dynamicQueues/test.celtix.jmstransport.queue\" " 
                        + "durableSubscriberName=\"Celtix_Queue_subscriber\" " 
                        + "useMessageIDAsCorrelationID=\"false\" />") >= 0);
    }

    public void testServiceExist() throws Exception {

        WSDLToServiceProcessor processor = new WSDLToServiceProcessor();

        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        env.put(ToolConstants.CFG_TRANSPORT, new String("http"));
        env.put(ToolConstants.CFG_SERVICE, new String("SOAPService_Test1"));
        env.put(ToolConstants.CFG_PORT, new String("SoapPort_Test1"));
        env.put(ToolConstants.CFG_BINDING_ATTR, new String("Greeter_SOAPBinding"));

        processor.setEnvironment(env);

        try {
            processor.process();
            fail("Do not catch expected tool exception for service and port exist!");
        } catch (Exception e) {
            if (!(e instanceof ToolException
                    && e.toString().indexOf(
                        "Input service and port already exist in imported contract.") >= 0)) {
                fail("Do not catch tool exception for service and port exist, "
                     + "catch other unexpected exception!");
            }
        }
    }

    public void testBindingNotExist() throws Exception {

        WSDLToServiceProcessor processor = new WSDLToServiceProcessor();

        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        env.put(ToolConstants.CFG_TRANSPORT, new String("http"));
        env.put(ToolConstants.CFG_BINDING_ATTR, new String("BindingNotExist"));
        env.put(ToolConstants.CFG_SERVICE, new String("serviceins"));
        env.put(ToolConstants.CFG_PORT, new String("portins"));

        processor.setEnvironment(env);

        try {
            processor.process();
            fail("Do not catch expected tool exception for  binding not exist!");
        } catch (Exception e) {
            if (!(e instanceof ToolException
                    && e.toString().indexOf("Input binding does not exist in imported contract.") >= 0)) {
                fail("Do not catch tool exception for binding not exist, "
                     + "catch other unexpected exception!");
            }
        }
    }

    private String getLocation(String wsdlFile) {
        return WSDLToServiceProcessorTest.class.getResource(wsdlFile).getFile();
    }
}
