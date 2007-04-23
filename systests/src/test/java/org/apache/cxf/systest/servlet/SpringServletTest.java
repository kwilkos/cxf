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

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.helpers.DOMUtils;
import org.junit.Test;

public class SpringServletTest extends AbstractServletTest {
    @Override
    protected String getConfiguration() {
        return "/org/apache/cxf/systest/servlet/web-spring.xml";
    }

    @Override
    protected Bus createBus() throws BusException {
        // don't set up the bus, let the servlet do it
        return null;
    }

    @Test
    public void testInvokingSpringBeans() throws Exception {

        WebRequest req = new PostMethodWebRequest("http://localhost/services/Greeter",
            getClass().getResourceAsStream("GreeterMessage.xml"),
            "text/xml; charset=utf-8");

        invokingEndpoint(req);
        
        req = new PostMethodWebRequest("http://localhost/services/Greeter1",
            getClass().getResourceAsStream("GreeterMessage.xml"), "text/xml; charset=utf-8");
        
        invokingEndpoint(req);
    }
    
    public void invokingEndpoint(WebRequest req) throws Exception {
        
        WebResponse response = newClient().getResponse(req);
        assertEquals("text/xml", response.getContentType());
        assertEquals("utf-8", response.getCharacterSet());

        Document doc = DOMUtils.readXml(response.getInputStream());
        assertNotNull(doc);

        addNamespace("h", "http://apache.org/hello_world_soap_http/types");
        assertValid("/s:Envelope/s:Body", doc);
        assertValid("//h:sayHiResponse", doc);
    }
}
