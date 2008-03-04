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

package org.apache.cxf.jaxrs.model;

import java.lang.reflect.Field;
import java.util.List;

import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;

public class ClassResourceInfoTest extends Assert {
    
    private static class TestClass {
        @HttpContext UriInfo u;
        @HttpContext HttpHeaders h;
        int i;
    }
    
    @Test
    public void testGetHttpContexts() {
        ClassResourceInfo c = new ClassResourceInfo(TestClass.class);
        List<Field> fields = c.getHttpContexts();
        assertEquals("Only root classes should check these fields", 0, fields.size());
        
        c = new ClassResourceInfo(TestClass.class, true);
        fields = c.getHttpContexts();
        assertEquals("2 http context fields available", 2, fields.size());
        assertTrue("Wrong fields selected", 
                   (fields.get(0).getType() == UriInfo.class
                   || fields.get(1).getType() == UriInfo.class)
                   && (fields.get(0).getType() == HttpHeaders.class
                   || fields.get(1).getType() == HttpHeaders.class));
        
    }
}
