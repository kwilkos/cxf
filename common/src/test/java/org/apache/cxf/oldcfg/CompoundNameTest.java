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

package org.apache.cxf.oldcfg;

import junit.framework.TestCase;

public class CompoundNameTest extends TestCase {
    
    public void testConstructors() {
        try {
            new CompoundName((String)null);
        } catch (ConfigurationException ex) {
            assertEquals("Unexpected exception code.", "INVALID_PART_EXC", ex.getCode()); 
        }
        
        assertNotNull(new CompoundName("cxf"));
        
        try {
            new CompoundName((String[])null);
        } catch (ConfigurationException ex) {
            assertEquals("Unexpected exception code.", "NO_PARTS_EXC", ex.getCode()); 
        } 
        try {
            new CompoundName(new String[] {});
        } catch (ConfigurationException ex) {
            assertEquals("Unexpected exception code.", "NO_PARTS_EXC", ex.getCode()); 
        } 
        
        CompoundName parent = new CompoundName(new String[] {"cxf"});
        assertNotNull(parent);
        
        try {
            new CompoundName(parent, null);
        } catch (ConfigurationException ex) {
            assertEquals("Unexpected exception code.", "INVALID_PART_EXC", ex.getCode()); 
        }
        
        assertNotNull(new CompoundName(parent, "xyz"));
    }
    
    public void testGetParentName() {
        CompoundName cn1 = new CompoundName("cxf");
        CompoundName cn2 = new CompoundName(cn1, "abc");
        assertNull(cn1.getParentName());
        assertEquals("Unexpected parent name.", cn1, cn2.getParentName());
    }
    
    public void testEquals() {
        CompoundName cn1 = new CompoundName("cxf");
        CompoundName cn2 = new CompoundName("cxf");
        CompoundName cn3 = new CompoundName(cn1, "abc");
        
        assertTrue(cn1.equals(cn1));
        
        assertTrue(!cn1.equals(this));
        
        assertTrue(cn1.equals(cn2));
        assertTrue(!cn1.equals(cn3));
    }
    
    public void testHashCode() {
        CompoundName cn1 = new CompoundName("cxf");
        CompoundName cn2 = new CompoundName("cxf");
        CompoundName cn3 = new CompoundName(cn1, "abc");
        
        assertEquals(cn1.hashCode(), cn1.hashCode());
        
        assertEquals(cn1.hashCode(), cn2.hashCode());
        assertTrue(cn1.hashCode() != cn3.hashCode());
    }
}
