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

package org.apache.cxf.systest.rest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;

import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;

public class RestClientServerHttpBindingTest extends ClientServerTestBase {
    private final QName serviceName = new QName("http://apache.org/hello_world_xml_http/wrapped",
                                                "XMLService");

    private final QName portName = new QName("http://apache.org/hello_world_xml_http/wrapped",
                                             "RestProviderPort");

    private final String endpointAddress =
        "http://localhost:9024/XMLService/RestProviderPort/Customer"; 
   
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(RestClientServerHttpBindingTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(HttpBindingServer.class));
            }
        };
    }    
   
    public void testHttpGET() throws Exception {
        URL url = new URL(endpointAddress + "?name=john&address=20");
        InputStream in = url.openStream();
        assertNotNull(in);       
    }

    public void testHttpPOSTDispatchHTTPBinding() throws Exception {
        Service service = Service.create(serviceName);
        service.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<Source> dispatcher = service.createDispatch(portName, Source.class, Service.Mode.MESSAGE);
        Map<String, Object> requestContext = dispatcher.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "POST");
        InputStream is = getClass().getResourceAsStream("resources/CustomerJohnReq.xml");
        Source result = dispatcher.invoke(new StreamSource(is));
        String tempstring = source2String(result);
        assertTrue("Result should start with Customer", tempstring.startsWith("<ns4:Customer"));
        assertTrue("Result should have CustomerID", tempstring.lastIndexOf("CustomerID>123456<") > 0);
    }
    
    public void testHttpGETDispatchHTTPBinding() throws Exception { 
        Service service = Service.create(serviceName); 
        URI endpointURI = new URI(endpointAddress);
        String path = null; 
        if (endpointURI != null) { 
            path = endpointURI.getPath(); 
        } 
        service.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<Source> d = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        Map<String, Object> requestContext = d.getRequestContext();
        Map<String, Object> responseContext = d.getResponseContext();
        
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "GET");
        requestContext.put(MessageContext.QUERY_STRING, "id=1"); 
        //this is the original path part of uri 
        requestContext.put(MessageContext.PATH_INFO, path);        
        Source result = d.invoke(null);
        
        // varify the responseContext;
        Map<String, List<String>> responseHeader =
            CastUtils.cast((Map)responseContext.get(MessageContext.HTTP_RESPONSE_HEADERS));
        assertNotNull("the response header should not be null", responseHeader);
        
        List<String> values = responseHeader.get("REST");
        assertNotNull("the response rest header should not be null", values);
        assertEquals("the list size wrong", 2, values.size());        
        assertNotNull("result shoud not be null", result);        
        String tempstring = source2String(result);
        assertTrue("Result should start with Customer", tempstring.startsWith("<ns4:Customer"));
        assertTrue("Result should have CustomerID", tempstring.lastIndexOf("CustomerID>123456<") > 0);
    }
    
    private String source2String(Source source) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(bos);
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        Properties oprops = new Properties();
        oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperties(oprops);
        trans.transform(source, sr);
        return bos.toString();
    }
}
