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
package org.apache.cxf.systest.servlet;


import org.w3c.dom.Document;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.helpers.DOMUtils;
import org.junit.Before;
import org.junit.Test;


public class CXFServletTest extends AbstractServletTest {
    
    
    @Before
    public void setUp() throws Exception {
        BusFactory.setDefaultBus(null);
        BusFactory.setThreadDefaultBus(null);
        super.setUp();
    }
    
    @Override
    protected Bus createBus() throws BusException {
        return null;
    }
    
    @Test
    public void testPostInvokeServices() throws Exception {
        
        invoke("UTF-8");
        invoke("iso-8859-1");        
    }

    private void invoke(String encoding) throws Exception {        
        WebRequest req = new PostMethodWebRequest(CONTEXT_URL + "/services/greeter", 
            getClass().getResourceAsStream("GreeterMessage.xml"), 
            "text/xml; charset=" + encoding);
        
        ServletUnitClient client = newClient();
        WebResponse response = client.getResponse(req);
        client.setExceptionsThrownOnErrorStatus(false);

        assertEquals("text/xml", response.getContentType());
        assertEquals(encoding, response.getCharacterSet());

        Document doc = DOMUtils.readXml(response.getInputStream());
        assertNotNull(doc);

        addNamespace("h", "http://apache.org/hello_world_soap_http/types");

        assertValid("/s:Envelope/s:Body", doc);
        assertValid("//h:sayHiResponse", doc);    
    }
    
    @Test
    public void testGetServiceList() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(false);

        WebResponse res = client.getResponse(CONTEXT_URL + "/services");
        
        
        WebLink[] links = res.getLinks();
        assertEquals("There should get two links for the service", 2, links.length);
        assertEquals(CONTEXT_URL + "/services/greeter?wsdl", links[0].getURLString());       
        assertEquals(CONTEXT_URL + "/services/greeter2?wsdl", links[1].getURLString()); 
        assertEquals("text/html", res.getContentType());
        
        res = client.getResponse(CONTEXT_URL + "/services/");
       
        
        links = res.getLinks();
        assertEquals("There should get two links for the service", 2, links.length);
        assertEquals(CONTEXT_URL + "/services/greeter?wsdl", links[0].getURLString());
        assertEquals(CONTEXT_URL + "/services/greeter2?wsdl", links[1].getURLString()); 
        
        assertEquals("text/html", res.getContentType());
        
       
        // Ensure that the Bus is available for people doing an Endpoint.publish() or similar.
        assertNotNull(BusFactory.getDefaultBus(false));
    }
    
    @Test
    public void testGetWSDL() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(true);
        
        WebRequest req = new GetMethodQueryWebRequest(CONTEXT_URL + "/services/greeter?wsdl");
        
        WebResponse res = client.getResponse(req); 
        assertEquals(200, res.getResponseCode());
        assertEquals("text/xml", res.getContentType());
        assertTrue("the wsdl should contain the opertion greetMe",
                   res.getText().contains("<wsdl:operation name=\"greetMe\">"));
        assertTrue("the soap address should changed",
                   res.getText().contains("<soap:address location=\"" + CONTEXT_URL + "/services/greeter\""));
        
    }
    
    @Test
    public void testGetWSDLWithXMLBinding() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(true);
        
        WebRequest req = new GetMethodQueryWebRequest(CONTEXT_URL + "/services/greeter2?wsdl");
        
        WebResponse res = client.getResponse(req); 
        assertEquals(200, res.getResponseCode());
        assertEquals("text/xml", res.getContentType());
        assertTrue("the wsdl should contain the opertion greetMe",
                   res.getText().contains("<wsdl:operation name=\"greetMe\">"));
        assertTrue("the http address should changed",
                   res.getText().contains(CONTEXT_URL + "/services/greeter2\""));
        
    }

    @Test
    public void testInvalidServiceUrl() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(false);

        WebResponse res = client.getResponse(CONTEXT_URL + "/services/NoSuchService");
        assertEquals(404, res.getResponseCode());
        assertEquals("text/html", res.getContentType());
    }

    @Test
    public void testServiceWsdlNotFound() throws Exception {
        WebRequest req = new GetMethodWebRequest(CONTEXT_URL + "/services/NoSuchService?wsdl");

        expectErrorCode(req, 404, "Response code 404 required for invalid WSDL url.");
    }
    
    @Test
    public void testGetImportedXSD() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(true);

        WebRequest req 
            = new GetMethodQueryWebRequest(CONTEXT_URL + "/services/greeter?wsdl");
        WebResponse res = client.getResponse(req); 
        assertEquals(200, res.getResponseCode());
        String text = res.getText();
        assertEquals("text/xml", res.getContentType());
        assertTrue(text.contains(CONTEXT_URL + "/services/greeter?wsdl=test_import.xsd"));

        req = new GetMethodQueryWebRequest(CONTEXT_URL + "/services/greeter?wsdl=test_import.xsd");
        res = client.getResponse(req); 
        assertEquals(200, res.getResponseCode());
        text = res.getText();
        
        assertEquals("text/xml", res.getContentType());
        assertTrue("the xsd should contain the completType SimpleStruct",
                   text.contains("<complexType name=\"SimpleStruct\">"));
    }
}
