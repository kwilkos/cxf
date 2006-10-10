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

package org.apache.cxf.service.model;

import junit.framework.TestCase;

public class TypeInfoTest extends TestCase {
    
    private TypeInfo typeInfo;
    
    public void setUp() throws Exception {
        typeInfo = new TypeInfo(null);
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testConstructor() throws Exception {
        assertNull(typeInfo.getService());
    }
 
    public void testSchema() throws Exception {
        assertEquals(typeInfo.getSchemas().size(), 0);
        SchemaInfo schemaInfo = new SchemaInfo(typeInfo, "http://schema1");
        typeInfo.addSchema(schemaInfo);
        assertEquals(typeInfo.getSchemas().size(), 1);
        schemaInfo = typeInfo.getSchema("dummySchema");
        assertNull(schemaInfo);
        schemaInfo = typeInfo.getSchema("http://schema1");
        assertNotNull(schemaInfo);
        assertEquals(schemaInfo.getNamespaceURI(), "http://schema1");
        assertTrue(schemaInfo.getTypeInfo() == typeInfo);
        schemaInfo = new SchemaInfo(typeInfo, "http://schema2");
        typeInfo.addSchema(schemaInfo);
        assertEquals(typeInfo.getSchemas().size(), 2);
        assertEquals(typeInfo.getSchema("http://schema2").getNamespaceURI(), "http://schema2");
    }
}
