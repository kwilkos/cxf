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

package org.apache.cxf.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

@Provider
public class DOMSourceProvider implements 
    MessageBodyReader<DOMSource>, MessageBodyWriter<DOMSource> {

    public boolean isWriteable(Class<?> type) {
        return DOMSource.class.isAssignableFrom(type);
    }
    
    public boolean isReadable(Class<?> type) {
        return DOMSource.class.isAssignableFrom(type);
    }
    
    public DOMSource readFrom(Class<DOMSource> source, MediaType media,
                              MultivaluedMap<String, String> httpHeaders, InputStream is) throws IOException {
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();

        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        }

        return new DOMSource(doc);
    }

    public void writeTo(DOMSource source, MediaType media, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream os) throws IOException {
        StreamResult result = new StreamResult(os);
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer t = tf.newTransformer();
            t.transform(source, result);
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }
    
    public long getSize(DOMSource source) {
        return -1;
    }
}
