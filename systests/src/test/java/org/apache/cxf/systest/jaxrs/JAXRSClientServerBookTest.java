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


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;


public class JAXRSClientServerBookTest extends AbstractBusClientServerTestBase {

/*    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue("server did not launch correctly", launchServer(BookServer.class));
    }*/
   
    @Test
    public void testGetBooks() throws Exception {
        String endpointAddress =
            "http://localhost:9080/xml/bookstore"; 
        URL url = new URL(endpointAddress);
        InputStream in = url.openStream();
        assertNotNull(in);           

        InputStream expected = getClass()
            .getResourceAsStream("resources/expected_get_books.txt");

        //System.out.println("---" + getStringFromInputStream(in));
        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in)); 
    }
    
    @Test
    public void testGetBook123() throws Exception {
        String endpointAddress =
            "http://localhost:9080/xml/bookstore/books/123"; 
        URL url = new URL(endpointAddress);
        InputStream in = url.openStream();
        assertNotNull(in);           

        InputStream expected = getClass()
            .getResourceAsStream("resources/expected_get_book123.txt");

        //System.out.println("---" + getStringFromInputStream(in));
        assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in)); 
    }
    
    @Test
    public void testAddBook() throws Exception {
        String endpointAddress =
            "http://localhost:9080/xml/bookstore/books";

        String inputFile = getClass().getResource("resources/add_book.txt").getFile();         
        File input =  new File(inputFile);
        PostMethod post = new PostMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(post);
            assertEquals(200, result);
            //System.out.println("Response status code: " + result);
            //System.out.println("Response body: ");
            //System.out.println(post.getResponseBodyAsString());
            
            InputStream expected = getClass().getResourceAsStream("resources/expected_add_book.txt");
            
            assertEquals(getStringFromInputStream(expected), post.getResponseBodyAsString());
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }               
    }  
    
    @Test
    public void testUpdateBook() throws Exception {
        String endpointAddress =
            "http://localhost:9080/xml/bookstore/books";

        String inputFile = getClass().getResource("resources/update_book.txt").getFile();         
        File input =  new File(inputFile);
        PutMethod post = new PutMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(post);
            assertEquals(200, result);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            System.out.println(post.getResponseBodyAsString());
            
            //Verify result
            endpointAddress =
                "http://localhost:9080/xml/bookstore/books/123"; 
            URL url = new URL(endpointAddress);
            InputStream in = url.openStream();
            assertNotNull(in);           

            InputStream expected = getClass()
                .getResourceAsStream("resources/expected_update_book.txt");

            //System.out.println("---" + getStringFromInputStream(in));
            assertEquals(getStringFromInputStream(expected), getStringFromInputStream(in)); 
            
            //Roll back changes:
            String inputFile1 = getClass().getResource(
                                    "resources/expected_get_book123.txt").getFile();         
            File input1 =  new File(inputFile1);
            PutMethod post1 = new PutMethod(endpointAddress);
            RequestEntity entity1 = new FileRequestEntity(input1, "text/xml; charset=ISO-8859-1");
            post1.setRequestEntity(entity1);
            HttpClient httpclient1 = new HttpClient();
            httpclient1.executeMethod(post);
            
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }               
    }  
    
    @Test
    public void testUpdateBookFailed() throws Exception {
        String endpointAddress =
            "http://localhost:9080/xml/bookstore/books";

        String inputFile = getClass().getResource("resources/update_book_not_exist.txt").getFile();         
        File input =  new File(inputFile);
        PutMethod post = new PutMethod(endpointAddress);
        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        
        try {
            int result = httpclient.executeMethod(post);
            assertEquals(200, result);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            System.out.println(post.getResponseBodyAsString());
           
        } finally {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }               
    } 
    
    private String getStringFromInputStream(InputStream in) throws Exception {        
        CachedOutputStream bos = new CachedOutputStream();
        IOUtils.copy(in, bos);
        in.close();
        bos.close();
        //System.out.println(bos.getOut().toString());        
        return bos.getOut().toString();        
    }

}
