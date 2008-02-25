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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class JAXRSClientServerBookTest extends AbstractBusClientServerTestBase {

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(BookServer.class));
    }
    
    @Test
    public void testGetBook123() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/books/123"; 
        URL url = new URL(endpointAddress);
        URLConnection connect = url.openConnection();
        connect.addRequestProperty("Accept", "application/xml");
        InputStream in = connect.getInputStream();
        assertNotNull(in);           

        InputStream expected = getClass()
            .getResourceAsStream("resources/expected_get_book123.txt");

        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in));
        
        connect = url.openConnection();
        connect.addRequestProperty("Accept", "application/xml,application/json");
        in = connect.getInputStream();
        assertNotNull(in);           

        expected = getClass()
            .getResourceAsStream("resources/expected_get_book123json.txt");

        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in));
    }
    
    @Test
    public void testGetChapter() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/booksubresource/123/chapters/1"; 
        URL url = new URL(endpointAddress);
        URLConnection connect = url.openConnection();
        connect.addRequestProperty("Accept", "application/xml");
        InputStream in = connect.getInputStream();
        assertNotNull(in);           

        InputStream expected = getClass()
            .getResourceAsStream("resources/expected_get_chapter1.txt");

        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in)); 
    }
    
    @Test
    public void testGetBook123ReturnString() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/booknames/123"; 
        URL url = new URL(endpointAddress);
        InputStream in = url.openStream();
        assertNotNull(in);           

        InputStream expected = getClass()
            .getResourceAsStream("resources/expected_get_book123_returnstring.txt");

        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in)); 
    }
    
    @Test
    public void testGetBookNotFound() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/books/126"; 

        GetMethod get = new GetMethod(endpointAddress);
        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(get);
            assertEquals(500, result);
            
            InputStream expected = getClass().getResourceAsStream("resources/expected_get_book_notfound.txt");
            
            assertEquals(getStringFromInputStream(expected), get.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            get.releaseConnection();
        }  
    }
    
    @Test
    public void testAddBook() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/books";

        String inputFile = getClass().getResource("resources/add_book.txt").getFile();         
        File input =  new File(inputFile);
        PostMethod post = new PostMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(post);
            assertEquals(200, result);
            
            InputStream expected = getClass().getResourceAsStream("resources/expected_add_book.txt");
            
            assertEquals(getStringFromInputStream(expected), post.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }               
    }  
    
    @Test
    public void testUpdateBook() throws Exception {
        String endpointAddress = "http://localhost:9080/bookstore/books";

        String inputFile = getClass().getResource("resources/update_book.txt").getFile();
        File input = new File(inputFile);
        PutMethod put = new PutMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        put.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();

        try {
            int result = httpclient.executeMethod(put);
            assertEquals(200, result);
        } finally {
            // Release current connection to the connection pool once you are
            // done
            put.releaseConnection();
        }

        // Verify result
        endpointAddress = "http://localhost:9080/bookstore/books/123";
        URL url = new URL(endpointAddress);
        URLConnection connect = url.openConnection();
        connect.addRequestProperty("Accept", "application/xml");
        InputStream in = connect.getInputStream();
        assertNotNull(in);

        InputStream expected = getClass().getResourceAsStream("resources/expected_update_book.txt");

        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in));

        // Roll back changes:
        String inputFile1 = getClass().getResource("resources/expected_get_book123.txt").getFile();
        File input1 = new File(inputFile1);
        PutMethod put1 = new PutMethod(endpointAddress);
        RequestEntity entity1 = new FileRequestEntity(input1, "text/xml; charset=ISO-8859-1");
        put1.setRequestEntity(entity1);
        HttpClient httpclient1 = new HttpClient();

        try {
            int result = httpclient1.executeMethod(put);
            assertEquals(200, result);
        } finally {
            // Release current connection to the connection pool once you are
            // done
            put1.releaseConnection();
        }
    }  
    
    @Test
    public void testUpdateBookWithDom() throws Exception {
        String endpointAddress = "http://localhost:9080/bookstore/bookswithdom";

        String inputFile = getClass().getResource("resources/update_book.txt").getFile();
        File input = new File(inputFile);
        PutMethod put = new PutMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        put.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();

        try {
            int result = httpclient.executeMethod(put);
            assertEquals(200, result);
            System.out.println(put.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are
            // done
            put.releaseConnection();
        }
        
        InputStream expected = getClass().getResourceAsStream("resources/update_book.txt");

        assertTrue(put.getResponseBodyAsString().indexOf(getStringFromInputStream(expected)) >= 0);
    }
    
    @Test
    public void testUpdateBookWithJSON() throws Exception {
        String endpointAddress = "http://localhost:9080/bookstore/bookswithjson";

        String inputFile = getClass().getResource("resources/update_book_json.txt").getFile();
        File input = new File(inputFile);
        PutMethod put = new PutMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "application/json; charset=ISO-8859-1");
        put.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();

        try {
            int result = httpclient.executeMethod(put);
            assertEquals(200, result);
        } finally {
            // Release current connection to the connection pool once you are
            // done
            put.releaseConnection();
        }

        // Verify result
        endpointAddress = "http://localhost:9080/bookstore/books/123";
        URL url = new URL(endpointAddress);
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("Accept", "application/xml");
        InputStream in = connection.getInputStream();
        assertNotNull(in);

        InputStream expected = getClass().getResourceAsStream("resources/expected_update_book.txt");

        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in));

        // Roll back changes:
        String inputFile1 = getClass().getResource("resources/expected_get_book123.txt").getFile();
        File input1 = new File(inputFile1);
        PutMethod put1 = new PutMethod(endpointAddress);
        RequestEntity entity1 = new FileRequestEntity(input1, "text/xml; charset=ISO-8859-1");
        put1.setRequestEntity(entity1);
        HttpClient httpclient1 = new HttpClient();

        try {
            int result = httpclient1.executeMethod(put);
            assertEquals(200, result);
        } finally {
            // Release current connection to the connection pool once you are
            // done
            put1.releaseConnection();
        }
    } 
    
    @Test
    public void testUpdateBookFailed() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/books";

        String inputFile = getClass().getResource("resources/update_book_not_exist.txt").getFile();         
        File input =  new File(inputFile);
        PutMethod post = new PutMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(post);
            assertEquals(304, result);
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }               
    } 
        
    @Test
    public void testGetCDs() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/cds"; 
        URL url = new URL(endpointAddress);
        URLConnection connect = url.openConnection();
        connect.addRequestProperty("Accept", "application/xml");
        InputStream in = connect.getInputStream();
        assertNotNull(in);           

        InputStream expected123 = getClass().getResourceAsStream("resources/expected_get_cd.txt");
        InputStream expected124 = getClass().getResourceAsStream("resources/expected_get_cds124.txt");
        String result = getStringFromInputStream(in);
        //System.out.println("---" + getStringFromInputStream(in));
        
        assertTrue(result.indexOf(getStringFromInputStream(expected123)) >= 0);
        assertTrue(result.indexOf(getStringFromInputStream(expected124)) >= 0);
    }
    
    @Test
    public void testGetCDJSON() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/cd/123"; 

        GetMethod get = new GetMethod(endpointAddress);
        get.addRequestHeader("Accept" , "application/json");

        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(get);
            assertEquals(200, result);

            InputStream expected = getClass().getResourceAsStream("resources/expected_get_cdjson.txt");
            
            assertEquals(getStringFromInputStream(expected), get.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            get.releaseConnection();
        }  
    }    
    
    @Test
    public void testGetCDsJSON() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/cds"; 

        GetMethod get = new GetMethod(endpointAddress);
        get.addRequestHeader("Accept" , "application/json");

        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(get);
            assertEquals(200, result);

            InputStream expected123 = getClass().getResourceAsStream("resources/expected_get_cdsjson123.txt");
            InputStream expected124 = getClass().getResourceAsStream("resources/expected_get_cdsjson124.txt");
            
            assertTrue(get.getResponseBodyAsString().indexOf(getStringFromInputStream(expected123)) >= 0);
            assertTrue(get.getResponseBodyAsString().indexOf(getStringFromInputStream(expected124)) >= 0);

        } finally {
            // Release current connection to the connection pool once you are done
            get.releaseConnection();
        }  
    }  
    
    @Test
    public void testGetCDXML() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/cd/123"; 

        GetMethod get = new GetMethod(endpointAddress);
        get.addRequestHeader("Accept" , "application/xml");

        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(get);
            assertEquals(200, result);

            InputStream expected = getClass().getResourceAsStream("resources/expected_get_cd.txt");
            
            assertEquals(getStringFromInputStream(expected), get.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            get.releaseConnection();
        }  
    }
    
    
    @Test
    public void testGetCDWithMultiContentTypesXML() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/cdwithmultitypes/123"; 

        GetMethod get = new GetMethod(endpointAddress);
        get.addRequestHeader("Accept" , "application/xml");

        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(get);
            assertEquals(200, result);

            InputStream expected = getClass().getResourceAsStream("resources/expected_get_cd.txt");
            
            assertEquals(getStringFromInputStream(expected), get.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            get.releaseConnection();
        }  
    }
    
    @Test
    public void testGetCDWithMultiContentTypesJSON() throws Exception {
        String endpointAddress =
            "http://localhost:9080/bookstore/cdwithmultitypes/123"; 

        GetMethod get = new GetMethod(endpointAddress);
        get.addRequestHeader("Accept" , "application/json");

        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(get);
            assertEquals(200, result);

            InputStream expected = getClass().getResourceAsStream("resources/expected_get_cdjson.txt");
            
            assertEquals(getStringFromInputStream(expected), get.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            get.releaseConnection();
        }  
    }
    
    private String getStringFromInputStream(InputStream in) throws Exception {        
        CachedOutputStream bos = new CachedOutputStream();
        IOUtils.copy(in, bos);
        in.close();
        bos.close();
        return bos.getOut().toString();        
    }

}