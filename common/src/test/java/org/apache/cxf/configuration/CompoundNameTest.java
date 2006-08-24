package org.apache.cxf.configuration;

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
