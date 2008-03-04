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

package org.apache.cxf.systest.jaxrs;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class AtomClientBookTest extends AbstractBusClientServerTestBase {

    private Abdera abdera = new Abdera();
    
    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(AtomBookServer.class));
    }
    
    @Test
    public void testGetBooks() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/books/feed"; 
        URL url = new URL(endpointAddress);
        InputStream in = url.openStream();
        assertNotNull(in);           

        Document<Feed> doc = abdera.getParser().parse(in);
        Feed feed = doc.getRoot();
        assertEquals(endpointAddress, feed.getBaseUri().toString());
        assertEquals("Collection of Books", feed.getTitle());
        
        
        // add new book
        Book b = new Book();
        b.setId(256);
        b.setName("AtomBook");
        Entry e = createBookEntry(b);
        StringWriter w = new StringWriter();
        e.writeTo(w);
        
        PostMethod post = new PostMethod(endpointAddress);
        post.setRequestEntity(
             new StringRequestEntity(w.toString(), "application/atom+xml", null));
        HttpClient httpclient = new HttpClient();
        
        String location = null;
        try {
            int result = httpclient.executeMethod(post);
            assertEquals(201, result);
            location = post.getResponseHeader("Location").getValue();
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }         
        
        url = new URL(location);
        in = url.openStream();
        assertNotNull(in);
        
        Document<Entry> entryDoc = abdera.getParser().parse(in);
        Entry entry = entryDoc.getRoot();
        assertEquals(location, entry.getBaseUri().toString());
        assertEquals("AtomBook", entry.getTitle());
        
        in.close();
        
        // get existing book
        
        endpointAddress =
            "http://localhost:9080/bookstore/books/subresources/123"; 
        url = new URL(endpointAddress);
        in = url.openStream();
        assertNotNull(in);           

        entryDoc = abdera.getParser().parse(in);
        entry = entryDoc.getRoot();
        assertEquals("CXF in Action", entry.getTitle());
        
        in.close();
        
    }
    
    private Entry createBookEntry(Book b) throws Exception {
        Factory factory = Abdera.getNewFactory();
        JAXBContext jc = JAXBContext.newInstance(Book.class);
        
        Entry e = factory.getAbdera().newEntry();
        e.setTitle(b.getName());
        e.setId(Long.toString(b.getId()));
        
        
        StringWriter writer = new StringWriter();
        jc.createMarshaller().marshal(b, writer);
        
        e.setContentElement(factory.newContent());
        e.getContentElement().setContentType(Content.Type.XML);
        e.getContentElement().setValue(writer.toString());
        
        return e;
    }   
    
   
}
