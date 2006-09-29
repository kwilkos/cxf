/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.systest.ws.addressing;


import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.ProtocolException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.Names;
import org.apache.cxf.ws.addressing.soap.VersionTransformer;
import org.apache.cxf.wsdl.EndpointReferenceUtils;

import org.apache.hello_world_soap_http.BadRecordLitFault;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.NoSuchCodeLitFault;
import org.apache.hello_world_soap_http.SOAPService;

import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;


/**
 * Tests the addition of WS-Addressing Message Addressing Properties.
 */
public class MAPTest extends ClientServerTestBase implements VerificationCache {

    static final String INBOUND_KEY = "inbound";
    static final String OUTBOUND_KEY = "outbound";

    private static MAPVerifier mapVerifier;
    private static HeaderVerifier headerVerifier;

    private static final QName SERVICE_NAME = 
        new QName("http://apache.org/hello_world_soap_http", "SOAPServiceAddressing");
    private static final QName PORT_NAME =
        new QName("http://apache.org/hello_world_soap_http", "SoapPort");
    private static final String NOWHERE = "http://nowhere.nada.nothing.nought:5555";
    private static final String DECOUPLED = "http://localhost:9999/decoupled_endpoint";
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
                boolean inProcess = "Windows 2000".equals(System.getProperty("os.name"));
                assertTrue("server did not launch correctly", 
                           launchServer(Server.class, inProcess));
            }
            
            public void setUp() throws Exception {
                startServers();

                SpringBusFactory bf = new SpringBusFactory();
                Bus bus = bf.createBus("org/apache/cxf/systest/ws/addressing/cxf.xml");
                bf.setDefaultBus(bus);
                setBus(bus);

                mapVerifier = new MAPVerifier();
                headerVerifier = new HeaderVerifier();
                Interceptor[] interceptors = {mapVerifier, headerVerifier};
                addInterceptors(getBus().getInInterceptors(), interceptors);
                addInterceptors(getBus().getOutInterceptors(), interceptors);
                addInterceptors(getBus().getOutFaultInterceptors(), interceptors);
                addInterceptors(getBus().getInFaultInterceptors(), interceptors);
            }
            
            private void addInterceptors(List<Interceptor> chain,
                                         Interceptor[] interceptors) {
                for (int i = 0; i < interceptors.length; i++) {
                    chain.add(interceptors[i]);
                }
            }
        };
    }

    public void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        SOAPService service = new SOAPService(wsdl, SERVICE_NAME);
        greeter = (Greeter)service.getPort(PORT_NAME, Greeter.class);
        mapVerifier.verificationCache = this;
        headerVerifier.verificationCache = this;
    }
    
    public void tearDown() {
        verified = null;
    }

    //--Tests
    
    public void testImplicitMAPs() throws Exception {
        try {
            String greeting = greeter.greetMe("implicit1");
            assertEquals("unexpected response received from service", 
                         "Hello implicit1",
                         greeting);
            checkVerification();
            greeting = greeter.greetMe("implicit2");
            assertEquals("unexpected response received from service", 
                         "Hello implicit2",
                         greeting);
            checkVerification();
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void xtestExplicitMAPs() throws Exception {
        try {
            Map<String, Object> requestContext = 
                ((BindingProvider)greeter).getRequestContext();
            AddressingProperties maps = new AddressingPropertiesImpl();
            AttributedURIType id = 
                ContextUtils.getAttributedURI("urn:uuid:12345");
            maps.setMessageID(id);
            requestContext.put(CLIENT_ADDRESSING_PROPERTIES, maps);
            String greeting = greeter.greetMe("explicit1");
            assertEquals("unexpected response received from service", 
                         "Hello explicit1",
                         greeting);
            checkVerification();

            // the previous addition to the request context impacts
            // on all subsequent invocations on this proxy => a duplicate
            // message ID fault is expected
            try {
                greeter.greetMe("explicit2");
                fail("expected ProtocolException on duplicate message ID");
            } catch (ProtocolException pe) {
                assertTrue("expected duplicate message ID failure",
                           "Duplicate Message ID urn:uuid:12345".equals(pe.getMessage()));
                checkVerification();
            }

            // clearing the message ID ensure a duplicate is not sent
            maps.setMessageID(null);
            maps.setRelatesTo(ContextUtils.getRelatesTo(id.getValue()));
            greeting = greeter.greetMe("explicit3");
            assertEquals("unexpected response received from service", 
                         "Hello explicit3",
                         greeting);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void xtestFaultTo() throws Exception {
        try {
            String greeting = greeter.greetMe("warmup");
            assertEquals("unexpected response received from service", 
                         "Hello warmup",
                         greeting);
            checkVerification();

            Map<String, Object> requestContext = 
                ((BindingProvider)greeter).getRequestContext();
            AddressingProperties maps = new AddressingPropertiesImpl();
            maps.setReplyTo(EndpointReferenceUtils.getEndpointReference(NOWHERE));
            maps.setFaultTo(EndpointReferenceUtils.getEndpointReference(DECOUPLED));
            requestContext.put(CLIENT_ADDRESSING_PROPERTIES, maps);
            try {
                greeter.testDocLitFault("BadRecordLitFault");
                fail("expected fault from service");
            } catch (BadRecordLitFault brlf) {
                checkVerification();
            } catch (UndeclaredThrowableException ex) {
                throw (Exception)ex.getCause();
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void testOneway() throws Exception {
        try {
            greeter.greetMeOneWay("implicit_oneway1");
            checkVerification();
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }
    
    public void xtestApplicationFault() throws Exception {
        try {
            greeter.testDocLitFault("BadRecordLitFault");
            fail("expected fault from service");
        } catch (BadRecordLitFault brlf) {
            checkVerification();
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
        String greeting = greeter.greetMe("intra-fault");
        assertEquals("unexpected response received from service", 
                     "Hello intra-fault",
                     greeting);
        try {
            greeter.testDocLitFault("NoSuchCodeLitFault");
            fail("expected NoSuchCodeLitFault");
        } catch (NoSuchCodeLitFault nsclf) {
            checkVerification();
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    }

    public void testVersioning() throws Exception {
        try {
            // expect two MAPs instances versioned with 200408, i.e. for both 
            // the partial and full responses
            mapVerifier.expectedExposedAs.add(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
            mapVerifier.expectedExposedAs.add(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
            String greeting = greeter.greetMe("versioning1");
            assertEquals("unexpected response received from service", 
                         "Hello versioning1",
                         greeting);
            checkVerification();
            greeting = greeter.greetMe("versioning2");
            assertEquals("unexpected response received from service", 
                         "Hello versioning2",
                         greeting);
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
     * @param parial true if partial response
     * @return null if all expected headers present, otherwise an error string.
     */
    protected static String verifyHeaders(List<String> wsaHeaders, boolean partial) {
        //System.out.println("verifying headers: " + wsaHeaders);
        String ret = null;
        if (!wsaHeaders.contains(Names.WSA_MESSAGEID_NAME)) {
            ret = "expected MessageID header"; 
        }
        if (!wsaHeaders.contains(Names.WSA_TO_NAME)) {
            ret = "expected To header";
        }
       
        if (!(wsaHeaders.contains(Names.WSA_REPLYTO_NAME)
              || wsaHeaders.contains(Names.WSA_RELATESTO_NAME))) {
            ret = "expected ReplyTo or RelatesTo header";
        }
        if (partial) { 
            if (!wsaHeaders.contains(Names.WSA_FROM_NAME)) {
                ret = "expected From header";
            }
        } else {
            // REVISIT Action missing from full response
            //if (!wsaHeaders.contains(Names.WSA_ACTION_NAME)) {
            //    ret = "expected Action header";
            //}            
        }
        return ret;
    }

    private void checkVerification() {
        assertNull("MAP/Header verification failed: " + verified, verified);
    }
}

