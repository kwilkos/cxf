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
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.hello_world_xml_http.wrapped.XMLService;

public class RestClientServerTest extends ClientServerTestBase {
    private final QName serviceName = new QName("http://apache.org/hello_world_xml_http/wrapped",
                                                "XMLService");

    private final QName portName = new QName("http://apache.org/hello_world_xml_http/wrapped",
                                             "RestProviderPort");

    public static class Server extends TestServerBase {

        protected void run() {
            Object implementor = new RestSourcePayloadProvider();
            String address = "http://localhost:9023/XMLService/RestProviderPort/Customer";
            Endpoint.publish(address, implementor);
        }

        public static void main(String[] args) {
            try {
                Server s = new Server();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                System.out.println("done!");
            }
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(RestClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testHttpPOSTDispatch() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl");
        assertNotNull(wsdl);

        XMLService service = new XMLService(wsdl, serviceName);
        assertNotNull(service);

        InputStream is = getClass().getResourceAsStream("resources/CustomerJohnReq.xml");
        Document doc = XMLUtils.parse(is);
        DOMSource reqMsg = new DOMSource(doc);
        assertNotNull(reqMsg);

        Dispatch<DOMSource> disp = service.createDispatch(portName, DOMSource.class, Service.Mode.PAYLOAD);
        DOMSource result = disp.invoke(reqMsg);
        assertNotNull(result);

        Node respDoc = result.getNode();
        assertEquals("Customer", respDoc.getFirstChild().getLocalName());
    }

    public void testHttpGET() throws Exception {
        String endpointAddress = "http://localhost:9023/XMLService/RestProviderPort/Customer";
        URL url = new URL(endpointAddress + "?name=john&address=20");
        InputStream in = url.openStream();
        assertNotNull(in);
        //StreamSource source = new StreamSource(in);
        //printSource(source);

        /*
         * url = new URL(endpointAddress + "/num1/10/num2/20");
         * System.out.println("Invoking URL=" + url); process(url);
         */
    }

    // Service.addPort() is not supported yet
    /*
     * public void testHttpGETDispatcher() throws Exception { String
     * endpointAddress =
     * "http://localhost:9023/XMLService/RestProviderPort/Customer"; Service
     * service = Service.create(serviceName); URI endpointURI = new
     * URI(endpointAddress.toString()); String path = null; String query = null;
     * if (endpointURI != null){ path = endpointURI.getPath(); query =
     * endpointURI.getQuery(); } service.addPort(portName,
     * HTTPBinding.HTTP_BINDING, endpointAddress.toString()); Dispatch<Source>
     * d = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
     * Map<String, Object> requestContext = d.getRequestContext();
     * requestContext.put(Message.HTTP_REQUEST_METHOD, new String("GET"));
     * requestContext.put(Message.QUERY_STRING, "id=1"); //this is the original
     * path part of uri requestContext.put(Message.PATH_INFO, path);
     * System.out.println ("Invoking Restful GET Request with query string ");
     * Source result = d.invoke(null); printSource(result); }
     */

    void printSource(Source source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
            System.out.println("**** Response ******" + bos.toString());
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
