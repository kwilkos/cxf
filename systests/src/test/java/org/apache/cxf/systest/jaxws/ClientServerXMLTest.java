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

package org.apache.cxf.systest.jaxws;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.helpers.XPathUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.headers.HeaderTester;
import org.apache.headers.XMLHeaderService;
import org.apache.headers.types.InHeader;
import org.apache.headers.types.InHeaderResponse;
import org.apache.headers.types.InoutHeader;
import org.apache.headers.types.InoutHeaderResponse;
import org.apache.headers.types.OutHeader;
import org.apache.headers.types.OutHeaderResponse;
import org.apache.headers.types.SOAPHeaderData;
import org.apache.hello_world_xml_http.bare.Greeter;
import org.apache.hello_world_xml_http.bare.XMLService;
import org.apache.hello_world_xml_http.bare.types.MyComplexStructType;
import org.apache.hello_world_xml_http.wrapped.GreeterFaultImpl;
import org.apache.hello_world_xml_http.wrapped.PingMeFault;

public class ClientServerXMLTest extends ClientServerTestBase {

    private final QName barePortName = new QName("http://apache.org/hello_world_xml_http/bare", "XMLPort");

    private final QName wrapServiceName = new QName("http://apache.org/hello_world_xml_http/wrapped",
            "XMLService");

    private final QName wrapPortName = new QName("http://apache.org/hello_world_xml_http/wrapped", "XMLPort");

    private final QName wrapFakePortName = new QName("http://apache.org/hello_world_xml_http/wrapped",
            "FakePort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClientServerXMLTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(ServerXMLBinding.class));
            }
        };
    }

    public void testBareBasicConnection() throws Exception {

        XMLService service = new XMLService();
        assertNotNull(service);

        String response1 = "Hello ";
        String response2 = "Bonjour";
        try {
            Greeter greeter = service.getPort(barePortName, Greeter.class);
            String username = System.getProperty("user.name");
            String reply = greeter.greetMe(username);

            assertNotNull("no response received from service", reply);
            assertEquals(response1 + username, reply);

            reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);

            MyComplexStructType argument = new MyComplexStructType();
            MyComplexStructType retVal = null;

            String str1 = "this is element 1";
            String str2 = "this is element 2";
            int int1 = 42;
            argument.setElem1(str1);
            argument.setElem2(str2);
            argument.setElem3(int1);
            retVal = greeter.sendReceiveData(argument);

            assertEquals(str1, retVal.getElem1());
            assertEquals(str2, retVal.getElem2());
            assertEquals(int1, retVal.getElem3());

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    public void testBareGetGreetMe() throws Exception {
        HttpURLConnection httpConnection =
            getHttpConnection("http://localhost:9031/XMLService/XMLPort/greetMe/requestType/cxf");
        httpConnection.connect();

        assertEquals(200, httpConnection.getResponseCode());

        assertEquals("text/xml", httpConnection.getContentType());
        assertEquals("OK", httpConnection.getResponseMessage());

        InputStream in = httpConnection.getInputStream();
        assertNotNull(in);

        Document doc = XMLUtils.parse(in);
        assertNotNull(doc);

        Map<String, String> ns = new HashMap<String, String>();
        ns.put("ns2", "http://apache.org/hello_world_xml_http/bare/types");
        XPathUtils xu = new XPathUtils(ns);
        String response = (String) xu.getValue("//ns2:responseType/text()", doc, XPathConstants.STRING);
        assertEquals("Hello cxf", response);
    }

    public void testWrapBasicConnection() throws Exception {

        org.apache.hello_world_xml_http.wrapped.XMLService service =
            new org.apache.hello_world_xml_http.wrapped.XMLService(
                this.getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl"), wrapServiceName);
        assertNotNull(service);

        String response1 = new String("Hello ");
        String response2 = new String("Bonjour");
        try {
            org.apache.hello_world_xml_http.wrapped.Greeter greeter = service.getPort(wrapPortName,
                    org.apache.hello_world_xml_http.wrapped.Greeter.class);
            String username = System.getProperty("user.name");
            String reply = greeter.greetMe(username);

            assertNotNull("no response received from service", reply);
            assertEquals(response1 + username, reply);

            reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);

            greeter.greetMeOneWay(System.getProperty("user.name"));

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    public void testAddPort() throws Exception {

        Service service = Service.create(wrapServiceName);
        service.addPort(wrapFakePortName, "http://cxf.apache.org/bindings/xformat",
                "http://localhost:9032/XMLService/XMLPort");
        assertNotNull(service);

        String response1 = new String("Hello ");
        String response2 = new String("Bonjour");

        org.apache.hello_world_xml_http.wrapped.Greeter greeter = service.getPort(wrapPortName,
                org.apache.hello_world_xml_http.wrapped.Greeter.class);

        try {
            String username = System.getProperty("user.name");
            String reply = greeter.greetMe(username);

            assertNotNull("no response received from service", reply);
            assertEquals(response1 + username, reply);

            reply = greeter.sayHi();
            assertNotNull("no response received from service", reply);
            assertEquals(response2, reply);

            greeter.greetMeOneWay(System.getProperty("user.name"));

        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
        BindingProvider bp = (BindingProvider) greeter;
        Map<String, Object> responseContext = bp.getResponseContext();
        Integer responseCode = (Integer) responseContext.get(Message.RESPONSE_CODE);
        assertEquals(200, responseCode.intValue());
    }

    public void testXMLFault() throws Exception {
        org.apache.hello_world_xml_http.wrapped.XMLService service =
            new org.apache.hello_world_xml_http.wrapped.XMLService(
                this.getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl"), wrapServiceName);
        assertNotNull(service);
        org.apache.hello_world_xml_http.wrapped.Greeter greeter = service.getPort(wrapPortName,
                org.apache.hello_world_xml_http.wrapped.Greeter.class);
        try {
            greeter.pingMe();
            fail("did not catch expected PingMeFault exception");
        } catch (PingMeFault ex) {
            assertEquals("minor value", 1, ex.getFaultInfo().getMinor());
            assertEquals("major value", 2, ex.getFaultInfo().getMajor());

            BindingProvider bp = (BindingProvider) greeter;
            Map<String, Object> responseContext = bp.getResponseContext();
            String contentType = (String) responseContext.get(Message.CONTENT_TYPE);
            assertEquals("text/xml", contentType);
            Integer responseCode = (Integer) responseContext.get(Message.RESPONSE_CODE);
            assertEquals(500, responseCode.intValue());
        }

        org.apache.hello_world_xml_http.wrapped.Greeter greeterFault = service.getXMLFaultPort();
        try {
            greeterFault.pingMe();
            fail("did not catch expected runtime exception");
        } catch (Exception ex) {
            assertTrue("check expected message of exception", ex.getMessage().indexOf(
                    GreeterFaultImpl.RUNTIME_EXCEPTION_MESSAGE) >= 0);
        }
    }

    public void testXMLBindingOfSoapHeaderWSDL() throws Exception {
        XMLHeaderService service = new XMLHeaderService();
        HeaderTester port = service.getXMLPort9000();
        try {
            verifyInHeader(port);
            verifyInOutHeader(port);
            verifyOutHeader(port);
        } catch (UndeclaredThrowableException ex) {
            throw (Exception) ex.getCause();
        }
    }

    public void verifyInHeader(HeaderTester proxy) throws Exception {
        InHeader me = new InHeader();
        me.setRequestType("InHeaderRequest");
        SOAPHeaderData headerInfo = new SOAPHeaderData();
        headerInfo.setMessage("message");
        headerInfo.setOriginator("originator");
        InHeaderResponse resp = proxy.inHeader(me, headerInfo);
        assertNotNull(resp);
        assertEquals("check returned response type", "requestType=InHeaderRequest"
                    + "\nheaderData.message=message" + "\nheaderData.getOriginator=originator",
                    resp.getResponseType());
    }

    public void verifyInOutHeader(HeaderTester proxy) throws Exception {
        InoutHeader me = new InoutHeader();
        me.setRequestType("InoutHeaderRequest");
        SOAPHeaderData headerInfo = new SOAPHeaderData();
        headerInfo.setMessage("inoutMessage");
        headerInfo.setOriginator("inoutOriginator");
        Holder<SOAPHeaderData> holder = new Holder<SOAPHeaderData>();
        holder.value = headerInfo;
        InoutHeaderResponse resp = proxy.inoutHeader(me, holder);
        assertNotNull(resp);
        assertEquals("check return value",
                     "requestType=InoutHeaderRequest",
                     resp.getResponseType());
        
        assertEquals("check inout value",
                     "message=inoutMessage",
                     holder.value.getMessage());
        assertEquals("check inout value",
                     "orginator=inoutOriginator",
                     holder.value.getOriginator());        
    }

    public void verifyOutHeader(HeaderTester proxy) throws Exception {
        OutHeader me = new OutHeader();
        me.setRequestType("OutHeaderRequest");
        
        Holder<OutHeaderResponse> outHeaderHolder = new Holder<OutHeaderResponse>();
        Holder<SOAPHeaderData> soapHeaderHolder = new Holder<SOAPHeaderData>();
        proxy.outHeader(me, outHeaderHolder, soapHeaderHolder);
        assertNotNull(outHeaderHolder.value);
        assertNotNull(soapHeaderHolder.value);
        assertEquals("check out value",
                     "requestType=OutHeaderRequest",
                     outHeaderHolder.value.getResponseType());
        
        assertEquals("check out value",
                     "message=outMessage",
                     soapHeaderHolder.value.getMessage());

        assertEquals("check out value",
                     "orginator=outOriginator",
                     soapHeaderHolder.value.getOriginator());
        
    }

}
