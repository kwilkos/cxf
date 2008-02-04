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
package org.apache.cxf.jaxrs;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ProduceMime;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JAXRSUtilsTest extends Assert {

    public class Customer {
        @ProduceMime("text/xml")   
        public void test() {
            // complete
        }
    };
    
    @Before
    public void setUp() {
    }

    @Test
    public void testFindTargetResourceClass() throws Exception {
        JAXRSServiceFactoryBean sf = new JAXRSServiceFactoryBean();
        sf.setResourceClasses(org.apache.cxf.jaxrs.resources.BookStoreNoSubResource.class);
        sf.create();        
        List<ClassResourceInfo> resources = ((JAXRSServiceImpl)sf.getService()).getClassResourceInfos();

        Map<String, String> values = new HashMap<String, String>(); 
        String contentTypes = "*/*";
        String acceptContentTypes = "*/*";

        //If acceptContentTypes does not specify a specific Mime type, the  
        //method is declared with a most specific ProduceMime type is selected.
        OperationResourceInfo ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123/",
                                                                       "GET", values, contentTypes,
                                                                       acceptContentTypes);       
        assertNotNull(ori);
        assertEquals("getBookJSON", ori.getMethod().getName());
        
        //test
        acceptContentTypes = "application/json";
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123/",
                                                                       "GET", values, contentTypes,
                                                                       acceptContentTypes);        
        assertNotNull(ori);
        assertEquals("getBookJSON", ori.getMethod().getName());
        
        //test 
        acceptContentTypes = "application/xml";
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123/",
                                                                       "GET", values, contentTypes,
                                                                       acceptContentTypes);        
        assertNotNull(ori);
        assertEquals("getBook", ori.getMethod().getName());
        
        //test find POST
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "POST", values, contentTypes,
                                                                       acceptContentTypes);       
        assertNotNull(ori);
        assertEquals("addBook", ori.getMethod().getName());
        
        //test find PUT
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "PUT", values, contentTypes,
                                                                       acceptContentTypes);  
        assertEquals("updateBook", ori.getMethod().getName());
        
        //test find DELETE
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123",
                                                                       "DELETE", values, contentTypes,
                                                                       acceptContentTypes);        
        assertNotNull(ori);
        assertEquals("deleteBook", ori.getMethod().getName());     
        
    }
    
    @Test
    @org.junit.Ignore
    public void testFindTargetResourceClassWithSubResource() throws Exception {
        JAXRSServiceFactoryBean sf = new JAXRSServiceFactoryBean();
        sf.setResourceClasses(org.apache.cxf.jaxrs.resources.BookStore.class);
        sf.create();        
        List<ClassResourceInfo> resources = ((JAXRSServiceImpl)sf.getService()).getClassResourceInfos();

        Map<String, String> values = new HashMap<String, String>(); 
        String contentTypes = "*/*";
        String acceptContentTypes = "*/*";

        OperationResourceInfo ori = JAXRSUtils.findTargetResourceClass(resources,
                                                                       "/bookstore/books/123/chapter/1",
                                                                       "GET", values, contentTypes,
                                                                       acceptContentTypes);       
        assertNotNull(ori);
        assertEquals("getBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "POST", values, contentTypes,
                                                                       acceptContentTypes);      
        assertNotNull(ori);
        assertEquals("addBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "PUT", values, contentTypes,
                                                                       acceptContentTypes);        
        assertNotNull(ori);
        assertEquals("updateBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123",
                                                                       "DELETE", values, contentTypes,
                                                                       acceptContentTypes);        
        assertNotNull(ori);
        assertEquals("deleteBook", ori.getMethod().getName());
    }

    @Test
    public void testIntersectMimeTypes() throws Exception {
        //test basic
        String[] methodMimeTypes = new String[]{"application/mytype", "application/xml", "application/json"};
        String acceptContentType = "application/json";
        String[] candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(1, candidateList.length);
        assertTrue(candidateList[0].equals("application/json"));
        
        //test basic       
        methodMimeTypes = new String[]{"application/mytype", "application/json", "application/xml"};
        acceptContentType = "application/json";
        candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(1, candidateList.length);
        assertTrue(candidateList[0].equals("application/json"));
        
        //test accept wild card */*       
        methodMimeTypes = new String[]{"application/mytype", "application/json", "application/xml"};
        acceptContentType = "*/*";
        candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(3, candidateList.length);
        
        //test accept wild card application/*       
        methodMimeTypes = new String[]{"text/html", "text/xml", "application/xml"};
        acceptContentType = "text/*";
        candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(2, candidateList.length);
        for (String type : candidateList) {
            assertTrue("text/html".equals(type) || "text/xml".equals(type));            
        }
        
        //test produce wild card */*
        methodMimeTypes = new String[]{"*/*"};
        acceptContentType = "application/json";
        candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(1, candidateList.length);
        assertTrue("application/json".equals(candidateList[0]));
        
        //test produce wild card application/*
        methodMimeTypes = new String[]{"application/*"};
        acceptContentType = "application/json";
        candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(1, candidateList.length);
        assertTrue("application/json".equals(candidateList[0]));        
        
        //test produce wild card */*, accept wild card */*
        methodMimeTypes = new String[]{"*/*"};
        acceptContentType = "*/*";
        candidateList = JAXRSUtils.intersectMimeTypes(methodMimeTypes, acceptContentType);  

        assertEquals(1, candidateList.length);
        assertTrue("*/*".equals(candidateList[0]));
    }
    
    @Test
    public void testIntersectMimeTypesTwoArray() throws Exception {
        //test basic
        String[] acceptedMimeTypes = new String[] {"application/mytype", "application/xml",
                                                   "application/json"};
        String[] providerMimeTypes = new String[] {"*/*"};

        String[] candidateList = JAXRSUtils.intersectMimeTypes(acceptedMimeTypes, providerMimeTypes);

        assertEquals(3, candidateList.length);
        for (String type : candidateList) {
            assertTrue("application/mytype".equals(type) || "application/xml".equals(type)
                       || "application/json".equals(type));
        }
        
        //test basic
        acceptedMimeTypes = new String[] {"*/*"};
        providerMimeTypes = new String[] {"application/mytype", "application/xml", "application/json"};

        candidateList = JAXRSUtils.intersectMimeTypes(acceptedMimeTypes, providerMimeTypes);

        assertEquals(3, candidateList.length);
        for (String type : candidateList) {
            assertTrue("application/mytype".equals(type) || "application/xml".equals(type)
                       || "application/json".equals(type));
        }
        
        //test empty
        acceptedMimeTypes = new String[] {"application/mytype", "application/xml"};
        providerMimeTypes = new String[] {"application/json"};

        candidateList = JAXRSUtils.intersectMimeTypes(acceptedMimeTypes, providerMimeTypes);

        assertEquals(0, candidateList.length);
    }
    
    @Test
    public void testAcceptTypesMatch() throws Exception {
        
        Method m = Customer.class.getMethod("test", new Class[]{});
        
        assertTrue("Accept types with multiple values can not be matched properly",
                   JAXRSUtils.matchMimeTypes(null, "text/xml,text/bar", m));
        assertTrue("Accept types with multiple values can not be matched properly",
                   JAXRSUtils.matchMimeTypes(null, "text/foo, text/bar, text/xml ", m));
        assertTrue("Accept types with multiple values can not be matched properly",
                   JAXRSUtils.matchMimeTypes(null, "text/bar,text/xml", m));
        assertTrue("Accept types with multiple values can not be matched properly",
                   JAXRSUtils.matchMimeTypes(null, "text/*", m));
        assertTrue("Accept types with multiple values can not be matched properly",
                   JAXRSUtils.matchMimeTypes(null, "*/*", m));
        assertTrue("Accept types with multiple values can not be matched properly",
                   JAXRSUtils.matchMimeTypes(null, null, m));
        
        
    }
}
