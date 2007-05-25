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

package org.apache.cxf.systest.provider;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.Endpoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class AttachmentProviderXMLClientServerTest extends AbstractBusClientServerTestBase {

    public static class Server extends AbstractBusTestServerBase {

        protected void run() {
            Object implementor = new AttachmentStreamSourceXMLProvider();
            String address = "http://localhost:9033/XMLServiceAttachment";
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

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly",
                launchServer(Server.class, true));
    }

    @Test
    public void testRequestWithAttachment() throws Exception {
        
        HttpURLConnection connection =  
            (HttpURLConnection)new URL("http://localhost:9033/XMLServiceAttachment").openConnection();
        connection.setRequestMethod("POST");
        
        String ct = "multipart/related; type=\"text/xml\"; " + "start=\"rootPart\"; "
                    + "boundary=\"----=_Part_4_701508.1145579811786\"";
        connection.addRequestProperty("Content-Type", ct);
        
        connection.setDoOutput(true);
        
        InputStream is = getClass().getResourceAsStream("attachmentData");
        IOUtils.copy(is, connection.getOutputStream());
        connection.getOutputStream().close();
        is.close();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        assertEquals("wrong content type", "application/xml+custom", connection.getContentType());
        Document result = builder.parse(connection.getInputStream());
        assertNotNull("result must not be null", result);
        
        connection.getInputStream().close();
        
        NodeList resList = result.getDocumentElement().getElementsByTagName("att");
        assertEquals("Two attachments must've been encoded", 2, resList.getLength());
        
        verifyAttachment(resList, "foo", "foobar");
        verifyAttachment(resList, "bar", "barbaz");
    }

    private void verifyAttachment(NodeList atts, String contentId, String value) {

        for (int i = 0; i < atts.getLength(); i++) {
            Element expElem = (Element)atts.item(i);
            String child = expElem.getFirstChild().getNodeValue();
            String contentIdVal = expElem.getAttribute("contentId");
            if (contentId.equals(contentIdVal)
                && (Base64Utility.encode(value.getBytes()).equals(child)
                    || Base64Utility.encode((value + "\n").getBytes()).equals(child))) {
                return;    
            }
        }
        
        fail("No encoded attachment with id " + contentId + " found");
    }
}
