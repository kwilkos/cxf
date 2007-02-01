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

package org.apache.cxf.jaxb.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import junit.framework.TestCase;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.message.XMLMessage;

public class XMLMessageDataReaderTest extends TestCase {

    XMLMessageDataReader reader;

    public void setUp() throws Exception {
        reader = new XMLMessageDataReader(null);
        assertNotNull(reader);
    }

    public void tearDown() throws IOException {
    }

    public void testReadBare() throws Exception {
        String expected = "<ns4:tickerSymbol>CXF</ns4:tickerSymbol>";
        InputStream is = getTestStream("../resources/sayHiDocLitBareReq.xml");
        assertNotNull(is);

        MessageImpl msg = new MessageImpl();
        msg.setContent(InputStream.class, is);
        XMLMessage xmlMsg = new XMLMessage(msg);

        Object source = reader.read(null, xmlMsg, DOMSource.class);
        assertNotNull(source);
        assertTrue(XMLUtils.toString((Source)source).contains(expected));
    }

    public void testReadEmptyInputStream() throws Exception {
        InputStream is = getTestStream("../resources/emptyReq.xml");
        assertNotNull(is);

        MessageImpl msg = new MessageImpl();
        msg.setContent(InputStream.class, is);
        XMLMessage xmlMsg = new XMLMessage(msg);

        Object source = reader.read(null, xmlMsg, DOMSource.class);
        assertNull(source);
        
        msg.setContent(InputStream.class, null);
        source = reader.read(null, xmlMsg, DOMSource.class);
        assertNull(source);
    }

    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

}
