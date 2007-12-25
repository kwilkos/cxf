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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JAXRSUtilsTest extends Assert {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testFindTargetResourceClass() throws Exception {
        JAXRSServiceFactoryBean sf = new JAXRSServiceFactoryBean();
        sf.setResourceClasses(org.apache.cxf.jaxrs.resources.BookStoreNoSubResource.class);
        sf.create();        
        List<ClassResourceInfo> resources = ((JAXRSServiceImpl)sf.getService()).getClassResourceInfos();

        Map<String, String> values = new HashMap<String, String>(); 

        OperationResourceInfo ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123/",
                                                                       "GET", values);       
        assertNotNull(ori);
        assertEquals("getBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "POST", values);       
        assertNotNull(ori);
        assertEquals("addBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "PUT", values);       
        assertNotNull(ori);
        assertEquals("updateBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123",
                                                                       "DELETE", values);       
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

        OperationResourceInfo ori = JAXRSUtils.findTargetResourceClass(resources,
                                                                       "/bookstore/books/123/chapter/1",
                                                                       "GET", values);       
        assertNotNull(ori);
        assertEquals("getBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "POST", values);       
        assertNotNull(ori);
        assertEquals("addBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books",
                                                                       "PUT", values);       
        assertNotNull(ori);
        assertEquals("updateBook", ori.getMethod().getName());
        
        ori = JAXRSUtils.findTargetResourceClass(resources, "/bookstore/books/123",
                                                                       "DELETE", values);       
        assertNotNull(ori);
        assertEquals("deleteBook", ori.getMethod().getName());
    }

}
