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

package org.apache.cxf.staxutils;

import java.io.*;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

public class StaxUtilsTest extends TestCase {

    public void testFactoryCreation() {
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(getTestStream("./resources/amazon.xml"));
        assertTrue(reader != null);
    }

    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    public void testToNextElement() {
        String soapMessage = "./resources/sayHiRpcLiteralReq.xml";
        XMLStreamReader r = StaxUtils.createXMLStreamReader(getTestStream(soapMessage));
        DepthXMLStreamReader reader = new DepthXMLStreamReader(r);
        assertTrue(StaxUtils.toNextElement(reader));
        assertEquals("Envelope", reader.getLocalName());

        StaxUtils.nextEvent(reader);

        assertTrue(StaxUtils.toNextElement(reader));
        assertEquals("Body", reader.getLocalName());
    }
    
    public void testCopy() throws Exception {
        
        // do the stream copying
        String soapMessage = "./resources/headerSoapReq.xml";     
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamReader reader = StaxUtils.createXMLStreamReader(getTestStream(soapMessage));
        XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(baos);
        StaxUtils.copy(reader, writer);
        writer.flush();
        baos.flush();
           
        // write output to a string
        String output = baos.toString();
        
        // re-read the input xml doc to a string
        InputStreamReader inputStreamReader = new InputStreamReader(getTestStream(soapMessage));
        StringWriter stringWriter = new StringWriter();
        char[] buffer = new char[4096];
        int n = 0;
        while (-1 != (n = inputStreamReader.read(buffer))) {
            stringWriter.write(buffer, 0 , n);
        }
        String input = stringWriter.toString();
        
        // compare the input and output string
        assertEquals(input, output);
    }
}
