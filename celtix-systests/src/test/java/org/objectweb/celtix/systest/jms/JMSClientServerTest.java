package org.objectweb.celtix.systest.jms;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.bus.transports.jms.JMSConstants;
import org.objectweb.celtix.hello_world_jms.HelloWorldOneWayPort;
import org.objectweb.celtix.hello_world_jms.HelloWorldOneWayQueueService;
import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;
import org.objectweb.celtix.hello_world_jms.HelloWorldPubSubPort;
import org.objectweb.celtix.hello_world_jms.HelloWorldPubSubService;
import org.objectweb.celtix.hello_world_jms.HelloWorldService;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.transports.jms.context.JMSMessageHeadersType;
import org.objectweb.celtix.transports.jms.context.JMSPropertyType;

public class JMSClientServerTest extends ClientServerTestBase {

    private QName serviceName; 
    private QName portName;

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JMSClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                Map<String, String> props = new HashMap<String, String>();                
                if (System.getProperty("activemq.store.dir") != null) {
                    props.put("activemq.store.dir", System.getProperty("activemq.store.dir"));
                }
                props.put("java.util.logging.config.file", 
                          System.getProperty("java.util.logging.config.file"));
                
                assertTrue("server did not launch correctly", launchServer(EmbeddedJMSBrokerLauncher.class,
                                                                           props, null));
                //Thread.sleep(10000);
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }  

    public void testBasicConnection() throws Exception {
        serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", 
                                 "HelloWorldService");
        portName = new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldPort");
        URL wsdl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull(wsdl);

        HelloWorldService service = new HelloWorldService(wsdl, serviceName);
        assertNotNull(service);

        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {
            HelloWorldPortType greeter = service.getPort(portName, HelloWorldPortType.class);
            for (int idx = 0; idx < 5; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);

                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void testOneWayTopicConnection() throws Exception {
        serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", 
                                 "HelloWorldPubSubService");
        portName = new QName("http://celtix.objectweb.org/hello_world_jms", 
                             "HelloWorldPubSubPort");
        URL wsdl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull(wsdl);

        HelloWorldPubSubService service = new HelloWorldPubSubService(wsdl, serviceName);
        assertNotNull(service);

        try {
            HelloWorldPubSubPort greeter = service.getPort(portName, HelloWorldPubSubPort.class);
            for (int idx = 0; idx < 5; idx++) {
                greeter.greetMeOneWay("JMS:PubSub:Milestone-" + idx);
            }
            //Give some time to complete one-way calls.
            Thread.sleep(100L);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void testOneWayQueueConnection() throws Exception {
        serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", 
                                 "HelloWorldOneWayQueueService");
        portName = new QName("http://celtix.objectweb.org/hello_world_jms", 
                             "HelloWorldOneWayPort");
        URL wsdl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull(wsdl);

        HelloWorldOneWayQueueService service = new HelloWorldOneWayQueueService(wsdl, serviceName);
        assertNotNull(service);

        try {
            HelloWorldOneWayPort greeter = service.getPort(portName, HelloWorldOneWayPort.class);
            for (int idx = 0; idx < 5; idx++) {
                greeter.greetMeOneWay("JMS:Queue:Milestone-" + idx);
            }
            //Give some time to complete one-way calls.
            Thread.sleep(100L);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void testContextPropogation() throws Exception {
        serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms",
                                 "HelloWorldService");
        portName = new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldPort");
        URL wsdl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull(wsdl);

        HelloWorldService service = new HelloWorldService(wsdl, serviceName);
        assertNotNull(service);

        try {
            HelloWorldPortType greeter = service.getPort(portName, HelloWorldPortType.class);
            InvocationHandler handler  = Proxy.getInvocationHandler(greeter);
            BindingProvider  bp = null;
            
            if (handler instanceof BindingProvider) {
                bp = (BindingProvider) handler;
                System.out.println(bp.toString());
                Map<String, Object> requestContext = bp.getRequestContext();
                JMSMessageHeadersType requestHeader = new JMSMessageHeadersType();
                requestHeader.setJMSCorrelationID("JMS_SAMPLE_CORRELATION_ID");
                requestHeader.setJMSExpiration(3600000L);
                JMSPropertyType propType = new JMSPropertyType();
                propType.setName("Test.Prop");
                propType.setValue("mustReturn");
                requestHeader.getProperty().add(propType);
                requestContext.put(JMSConstants.JMS_CLIENT_REQUEST_HEADERS, requestHeader);
            } 
 
            String greeting = greeter.greetMe("Milestone-");
            assertNotNull("no response received from service", greeting);

            assertEquals("Hello Milestone-", greeting);

            if (bp != null) {
                Map<String, Object> responseContext = bp.getResponseContext();
                JMSMessageHeadersType responseHdr = 
                     (JMSMessageHeadersType) responseContext.get(JMSConstants.JMS_CLIENT_RESPONSE_HEADERS);
                if (responseHdr == null) {
                    fail("response Header should not be null");
                }
                
                assertTrue("CORRELATION ID should match :", 
                           "JMS_SAMPLE_CORRELATION_ID".equals(responseHdr.getJMSCorrelationID()));
                assertTrue("response Headers must conain the app specific property set by request context.", 
                           responseHdr.getProperty() != null);
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSClientServerTest.class);
    }
}
