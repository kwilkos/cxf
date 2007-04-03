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

import java.net.URL;

import org.w3c.dom.Document;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.hello_world_soap_http.GreeterImpl;
import org.junit.Test;


public class CXFServletTest extends AbstractServletTest {
    
    // Create the JaxWsService with the JaxWsServerFactoryBean
    protected void setupJaxwsService() {
        JaxWsServerFactoryBean svr = new JaxWsServerFactoryBean();
        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        svr.getServiceFactory().setWsdlURL(resource);
        svr.setBus(getBus());
        svr.setServiceClass(GreeterImpl.class);
        svr.setAddress("http://localhost/services/Greeter");
        GreeterImpl greeter = new GreeterImpl();
        BeanInvoker invoker = new BeanInvoker(greeter);
        svr.getServiceFactory().setInvoker(invoker);

        svr.create();
        
    }
    
    @Test
    public void testPostInvokeServices() throws Exception {
        setupJaxwsService();
        
        //Thread.sleep(6000000);
        //newClient();        
        invoke("UTF-8");
        invoke("iso-8859-1");        
    }

    private void invoke(String encoding) throws Exception {        
        WebRequest req = new PostMethodWebRequest("http://localhost/services/Greeter", 
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

        WebResponse res = client.getResponse("http://localhost/services");    
        
        WebLink[] links = res.getLinks();
        assertEquals("There should get one link for service", links.length, 1);
        assertEquals(links[0].getURLString(), "http://localhost/services/greeter?wsdl");       
        assertEquals("text/html", res.getContentType());
    }
    
    @Test
    public void testGetWSDL() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(true);
        
        WebRequest req = new GetMethodQueryWebRequest("http://localhost/services/greeter?wsdl");
        
        WebResponse res = client.getResponse(req); 
        assertEquals(200, res.getResponseCode());
        assertEquals("text/xml", res.getContentType());
        assertTrue("the wsdl should contain the opertion greetMe",
                   res.getText().contains("<wsdl:operation name=\"greetMe\">"));
    }

    @Test
    public void testInvalidServiceUrl() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(false);

        WebResponse res = client.getResponse("http://localhost/services/NoSuchService");
        assertEquals(404, res.getResponseCode());
        assertEquals("text/html", res.getContentType());
    }

    @Test
    public void testServiceWsdlNotFound() throws Exception {
        WebRequest req = new GetMethodWebRequest("http://localhost/services/NoSuchService?wsdl");

        expectErrorCode(req, 404, "Response code 404 required for invalid WSDL url.");
    }
    
    @Test
    public void testGetImportedXSD() throws Exception {
        ServletUnitClient client = newClient();
        client.setExceptionsThrownOnErrorStatus(true);

        WebRequest req = new GetMethodQueryWebRequest("http://localhost/services/greeter/test_import.xsd");
        
        WebResponse res = client.getResponse(req); 
        assertEquals(200, res.getResponseCode());
        //assertEquals("text/xml", res.getContentType());
        assertTrue("the xsd should contain the completType SimpleStruct",
                   res.getText().contains("<complexType name=\"SimpleStruct\">"));
    }
}
