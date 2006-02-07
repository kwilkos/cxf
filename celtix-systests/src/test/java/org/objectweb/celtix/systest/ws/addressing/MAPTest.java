package org.objectweb.celtix.systest.ws.addressing;


import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.Names;
import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;

import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;


/**
 * Tests the addition of WS-Addressing Message Addressing Properties.
 */
public class MAPTest extends ClientServerTestBase implements VerificationCache {
    
    static final String INBOUND_KEY = "inbound";
    static final String OUTBOUND_KEY = "outbound";
    private static final QName SERVICE_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http", "SOAPServiceAddressing");
    private static final QName PORT_NAME =
        new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");
    private static Map<Object, Map<String, String>> messageIDs =
        new HashMap<Object, Map<String, String>>();
    private Greeter greeter;
    private String verified;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MAPTest.class);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(MAPTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                // special case handling for WS-Addressing system test to avoid
                // UUID related issue when server is run as separate process
                // via maven on Win2k
                assertTrue("server did not launch correctly", 
                           launchServer(Server.class, "Windows 2000".equals(System.getProperty("os.name"))));
            }
            
            public void setUp() throws Exception {
                // set up configuration for decoupled response endpoint
                configFileName = "file:///"
                                 + System.getProperty("user.dir")
                                 + "/src/test/java/org/objectweb/celtix/systest/ws/addressing/client.xml";
                super.setUp();
            }
        };
    }  

    public void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        SOAPService service = new SOAPService(wsdl, SERVICE_NAME);
        greeter = (Greeter)service.getPort(PORT_NAME, Greeter.class);
        BindingProvider provider = (BindingProvider)greeter;        
        List<Handler> handlerChain = provider.getBinding().getHandlerChain();
        for (Object h : handlerChain) {
            if (h instanceof MAPVerifier) {
                ((MAPVerifier)h).verificationCache = this;
            } else if (h instanceof HeaderVerifier) {
                ((HeaderVerifier)h).verificationCache = this;
            } 
        }
    }

    public void tearDown() {
        verified = null;
    }

    //--Tests
    
    public void testImplicitMAPs() throws Exception {
        try {
            String greeting = greeter.greetMe("implicit1");
            assertNotNull("no response received from service", greeting);
            checkVerification();
            greeting = greeter.greetMe("implicit2");
            assertNotNull("no response received from service", greeting);
            checkVerification();
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testExplicitMAPs() throws Exception {
        try {
            Map<String, Object> requestContext = 
                ((BindingProvider)greeter).getRequestContext();
            AddressingProperties maps = new AddressingPropertiesImpl();
            AttributedURIType id = 
                ContextUtils.getAttributedURI("urn:uuid:12345");
            maps.setMessageID(id);
            requestContext.put(CLIENT_ADDRESSING_PROPERTIES, maps);
            String greeting = greeter.greetMe("explicit1");
            assertNotNull("no response received from service", greeting);
            checkVerification();

            // the previous addition to the request context impacts
            // on all subsequent invocations on this proxy => a duplicate
            // message ID fault is expected
            try {
                greeter.greetMe("explicit2");
                fail("expected ProtocolException on dulicate message ID");
            } catch (ProtocolException pe) {
                assertTrue("expected duplicate message ID failure",
                           "Duplicate Message ID urn:uuid:12345".equals(pe.getMessage()));
                checkVerification();
            }

            // clearing the message ID ensure a duplicate is not sent
            maps.setMessageID(null);
            maps.setRelatesTo(ContextUtils.getRelatesTo(id.getValue()));
            greeting = greeter.greetMe("explicit3");
            assertNotNull("no response received from service", greeting);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testFaultMAPs() throws Exception {
        try {
            greeter.testDocLitFault("BadRecordLitFault");
            fail("expected fault from service");
        } catch (BadRecordLitFault brlf) {
            checkVerification();
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    //--VerificationCache implementation

    public void put(String verification) {
        if (verification != null) {
            verified = verified == null
                       ? verification
                : verified + "; " + verification;
        }
    }

    //--Verification methods called by handlers

    /**
     * Verify presence of expected MAPs.
     *
     * @param maps the MAPs to verify
     * @param checkPoint the check point
     * @return null if all expected MAPs present, otherwise an error string.
     */
    protected static String verifyMAPs(AddressingProperties maps, 
                                       Object checkPoint) {
        if (maps == null) {
            return "expected MAPs";
        }
        //String rt = maps.getReplyTo() != null ? maps.getReplyTo().getAddress().getValue() : "null"; 
        //System.out.println("verifying MAPs: " + maps
        //                   + " id: " + maps.getMessageID().getValue() 
        //                   + " to: " + maps.getTo().getValue()
        //                   + " reply to: " + rt);
        // MessageID
        String id = maps.getMessageID().getValue();
        if (id == null) {
            return "expected MessageID MAP";
        }
        if (!id.startsWith("urn:uuid")) {
            return "bad URN format in MessageID MAP: " + id;
        }
        // ensure MessageID is unique for this check point
        Map<String, String> checkPointMessageIDs = messageIDs.get(checkPoint);
        if (checkPointMessageIDs != null) { 
            if (checkPointMessageIDs.containsKey(id)) {
                //return "MessageID MAP duplicate: " + id;
                return null;
            }
        } else {
            checkPointMessageIDs = new HashMap<String, String>();
            messageIDs.put(checkPoint, checkPointMessageIDs);    
        }
        checkPointMessageIDs.put(id, id);
        // To
        if (maps.getTo() == null) {
            return "expected To MAP";
        }
        return null;
    }

    /**
     * Verify presence of expected MAP headers.
     *
     * @param wsaHeaders a list of the wsa:* headers present in the SOAP
     * message
     * @return null if all expected headers present, otherwise an error string.
     */
    protected static String verifyHeaders(List<String> wsaHeaders) {
        //System.out.println("verifying headers: " + wsaHeaders);
        if (!wsaHeaders.contains(Names.WSA_MESSAGEID_NAME)) {
            return "expected MessageID header"; 
        }
        if (!wsaHeaders.contains(Names.WSA_TO_NAME)) {
            return "expected To header";
        }
        return null;
    }

    private void checkVerification() {
        assertNull("MAP/Header verification failed: " + verified, verified);
    }
}

