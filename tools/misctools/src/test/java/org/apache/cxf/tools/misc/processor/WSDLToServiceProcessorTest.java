package org.apache.cxf.tools.misc.processor;

import java.io.File;
import java.util.Iterator;

import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.extensions.jms.JMSAddress;
import org.apache.cxf.tools.misc.WSDLToService;


public class WSDLToServiceProcessorTest extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void testNewService() throws Exception {
        String[] args = new String[] {"-transport", "http", "-e", "serviceins", "-p", "portins", "-n",
                                      "Greeter_SOAPBinding", "-a",
                                      "http://localhost:9000/newservice/newport", "-d",
                                      output.getCanonicalPath(), getLocation("/wsdl/hello_world.wsdl")};
        WSDLToService.main(args);

        File outputFile = new File(output, "hello_world-service.wsdl");
        assertTrue("New wsdl file is not generated", outputFile.exists());
        WSDLToServiceProcessor processor = new WSDLToServiceProcessor();
        processor.setEnvironment(env);
        try {
            processor.parseWSDL(outputFile.getAbsolutePath());
            Service service = processor.getWSDLDefinition().getService(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "serviceins"));
            if (service == null) {
                fail("Element wsdl:service serviceins Missed!");
            }
            Iterator it = service.getPort("portins").getExtensibilityElements().iterator();
            if (service == null) {
                fail("Element wsdl:port portins Missed!");
            }
            boolean found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof SOAPAddress) {
                    SOAPAddress soapAddress = (SOAPAddress)obj;
                    if (soapAddress.getLocationURI() != null
                        && soapAddress.getLocationURI().equals("http://localhost:9000/newservice/newport")) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                fail("Element soap:address of service port Missed!");
            }
        } catch (ToolException e) {
            fail("Exception Encountered when parsing wsdl, error: " + e.getMessage());
        }
    }

    public void testDefaultLocation() throws Exception {

        String[] args = new String[] {"-transport", "http", "-e", "serviceins", "-p", "portins", "-n",
                                      "Greeter_SOAPBinding", "-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/hello_world.wsdl")};
        WSDLToService.main(args);

        File outputFile = new File(output, "hello_world-service.wsdl");
        assertTrue("New wsdl file is not generated", outputFile.exists());
        WSDLToServiceProcessor processor = new WSDLToServiceProcessor();
        processor.setEnvironment(env);
        try {
            processor.parseWSDL(outputFile.getAbsolutePath());
            Service service = processor.getWSDLDefinition().getService(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "serviceins"));
            if (service == null) {
                fail("Element wsdl:service serviceins Missed!");
            }
            Iterator it = service.getPort("portins").getExtensibilityElements().iterator();
            if (service == null) {
                fail("Element wsdl:port portins Missed!");
            }
            boolean found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof SOAPAddress) {
                    SOAPAddress soapAddress = (SOAPAddress)obj;
                    if (soapAddress.getLocationURI() != null
                        && soapAddress.getLocationURI().equals("http://localhost:9000/serviceins/portins")) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                fail("Element soap:address of service port Missed!");
            }
        } catch (ToolException e) {
            fail("Exception Encountered when parsing wsdl, error: " + e.getMessage());
        }
    }

    public void testJMSNewService() throws Exception {
        String[] args = new String[] {"-transport", "jms", "-e", "serviceins", "-p", "portins", "-n",
                                      "HelloWorldPortBinding", "-jpu", "tcp://localhost:61616", "-jcf",
                                      "org.activemq.jndi.ActiveMQInitialContextFactory", "-jfn",
                                      "ConnectionFactory", "-jdn",
                                      "dynamicQueues/test.celtix.jmstransport.queue", "-jmt", "text", "-jmc",
                                      "false", "-jsn", "Celtix_Queue_subscriber", "-d",
                                      output.getCanonicalPath(), getLocation("/wsdl/jms_test.wsdl")};
        WSDLToService.main(args);
        File outputFile = new File(output, "jms_test-service.wsdl");
        assertTrue("New wsdl file is not generated", outputFile.exists());
        WSDLToServiceProcessor processor = new WSDLToServiceProcessor();
        processor.setEnvironment(env);
        try {
            processor.parseWSDL(outputFile.getAbsolutePath());
            Service service = processor.getWSDLDefinition().getService(
                                                                       new QName(processor
                                                                           .getWSDLDefinition()
                                                                           .getTargetNamespace(),
                                                                                 "serviceins"));
            if (service == null) {
                fail("Element wsdl:service serviceins Missed!");
            }
            Iterator it = service.getPort("portins").getExtensibilityElements().iterator();
            if (service == null) {
                fail("Element wsdl:port portins Missed!");
            }
            boolean found = false;
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof JMSAddress) {
                    JMSAddress jmsAddress = (JMSAddress)obj;
                    if (!(jmsAddress.getDestinationStyle() != null && jmsAddress.getDestinationStyle()
                        .equals("queue"))) {
                        break;
                    }
                    if (!(jmsAddress.getDurableSubscriberName() != null && jmsAddress
                        .getDurableSubscriberName().equals("Celtix_Queue_subscriber"))) {
                        break;
                    }
                    if (!(jmsAddress.getInitialContextFactory() != null && jmsAddress
                        .getInitialContextFactory()
                        .equals("org.activemq.jndi.ActiveMQInitialContextFactory"))) {
                        break;
                    }
                    if (!(jmsAddress.getJndiDestinationName() != null && jmsAddress.getJndiDestinationName()
                        .equals("dynamicQueues/test.celtix.jmstransport.queue"))) {
                        break;
                    }
                    if (!(jmsAddress.getJndiProviderURL() != null && jmsAddress.getJndiProviderURL()
                        .equals("tcp://localhost:61616"))) {
                        break;
                    }
                    if (!(jmsAddress.getMessageType() != null && jmsAddress.getMessageType().equals("text") 
                        && !jmsAddress.isUseMessageIDAsCorrelationID())) {
                        break;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Element jms:address of service port Missed!");
            }
        } catch (ToolException e) {
            fail("Exception Encountered when parsing wsdl, error: " + e.getMessage());
        }
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
            fail("Do not catch expected tool exception for service and port exist");
        } catch (Exception e) {
            if (!(e instanceof ToolException && e.toString()
                .indexOf("Input service and port already exist in imported contract") >= 0)) {
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
            if (!(e instanceof ToolException && e.toString()
                .indexOf("Input binding does not exist in imported contract") >= 0)) {
                fail("Do not catch tool exception for binding not exist, "
                     + "catch other unexpected exception!");
            }
        }
    }

    private String getLocation(String wsdlFile) {
        return WSDLToServiceProcessorTest.class.getResource(wsdlFile).getFile();
    }
}
