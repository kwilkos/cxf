package org.objectweb.celtix.bus.configuration.spring;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;

public class BeanNameTest extends TestCase {
    
    public void testNormalisation() {
        
        String n = "*.*abc*efg...hij.klm***nop.*.qrs.?.xyz";
        String nn = "*abc*efg.hij.klm*nop*qrs.?.xyz";
        
        String nbn;
        nbn = new BeanName((String)null, true).getNormalisedName();
        assertEquals(nbn, null, nbn);
        nbn = new BeanName("", true).getNormalisedName();
        assertEquals(nbn, "", nbn);
        nbn = new BeanName("?", true).getNormalisedName();
        assertEquals(nbn, "?", nbn);
        nbn = new BeanName(".", true).getNormalisedName();
        assertEquals(nbn, ".", nbn);
        nbn = new BeanName("*", true).getNormalisedName();
        assertEquals(nbn, "*", nbn);
        nbn = new BeanName("a", true).getNormalisedName();
        assertEquals(nbn, "a", nbn);
        nbn = new BeanName("a.", true).getNormalisedName();
        assertEquals(nbn, "a.", nbn);
        nbn = new BeanName("a.b**", true).getNormalisedName();
        assertEquals(nbn, "a.b*", nbn);
        nbn = new BeanName("{a.b**}", true).getNormalisedName();
        assertEquals(nbn, "{a.b**}", nbn);
        nbn = new BeanName("x.{a.b**}", true).getNormalisedName();
        assertEquals(nbn, "x.{a.b**}", nbn);
        nbn = new BeanName("*{a.b**}", true).getNormalisedName();
        assertEquals(nbn, "*{a.b**}", nbn);
        nbn = new BeanName("*{a.b**}x.y", true).getNormalisedName();
        assertEquals(nbn, "*{a.b**}x.y", nbn);
 
        nbn = new BeanName(n, true).getNormalisedName();
        assertEquals(nbn, nn, nbn);

        BeanName bn = new BeanName(n, false);
        assertNull(bn.getNormalisedName());
        assertEquals(n, bn.getName());
        bn.normalise();
        assertEquals(nn, bn.getNormalisedName());
        assertEquals(n, bn.getName());
        
        bn = new BeanName(n, true);
        assertEquals(n, bn.getName());
        assertEquals(nn, bn.getNormalisedName());
        bn.normalise();
        assertEquals(n, bn.getName());
        assertEquals(nn, bn.getNormalisedName());
    }
    
    public void testConstruction() { 
        
        Configuration c = EasyMock.createMock(Configuration.class);
        CompoundName id = new CompoundName("a", "b", "c");
        c.getId();
        EasyMock.expectLastCall().andReturn(id);
       
        EasyMock.replay(c);
        
        BeanName bn = new BeanName(c);
        assertEquals(bn.getName(), "a.b.c", bn.getName());
        
        EasyMock.verify(c);
    }
    
    public void testIterator() {
        BeanName bn = new BeanName("", true);
        BeanName.ComponentIterator it = bn.getIterator();
        assertTrue(!it.hasNext());
        
        String n = "simple";
        bn = new BeanName(n, true);
        it = bn.getIterator();        
        assertEquals(0, it.lastBinding());
        assertTrue(it.hasNext());
        assertEquals(n, it.next());
        assertEquals(0, it.lastBinding());
        assertTrue(!it.hasNext());
        
        n = "{http://www.objectweb.org/handlers}AddNumbersService";
        bn = new BeanName(n, true);
        it = bn.getIterator();        
        assertEquals(0, it.lastBinding());
        assertTrue(it.hasNext());
        assertEquals(n, it.next());
        assertEquals(0, it.lastBinding());
        assertTrue(!it.hasNext());
        
        bn = new BeanName("one.two.three", true);
        it = bn.getIterator();
        assertEquals(0, it.lastBinding());
        assertTrue(it.hasNext());
        it.next();
        assertEquals(0, it.lastBinding());
        assertTrue(it.hasNext());
        it.next();
        assertEquals(BeanName.TIGHT_BINDING, it.lastBinding());
        assertTrue(it.hasNext());
        it.next();
        assertEquals(BeanName.TIGHT_BINDING, it.lastBinding());
        assertTrue(!it.hasNext());
        
        bn = new BeanName("one.{a.b.c}two.three", true);
        it = bn.getIterator();
        assertEquals(0, it.lastBinding());
        assertTrue(it.hasNext());
        it.next();
        assertEquals(0, it.lastBinding());
        assertTrue(it.hasNext());
        assertEquals("{a.b.c}two", it.next());
        assertEquals(BeanName.TIGHT_BINDING, it.lastBinding());
        assertTrue(it.hasNext());
        it.next();
        assertEquals(BeanName.TIGHT_BINDING, it.lastBinding());
        assertTrue(!it.hasNext());
    }
    
    public void testMatch() {
        
        BeanName[] matchingBeanNames = {
            new BeanName("one.two.three", true),
            new BeanName(".one.two.three", true),
            new BeanName("one.two.three.", true),
            new BeanName("one.?.three", true),
            new BeanName("?.?.three", true),
            new BeanName("*two.three", true),
            new BeanName("*three", true),
            new BeanName("one*three", true),                          
        };
        
        BeanName[] nonMatchingBeanNames = {
            new BeanName("simple", true),
        };
        
        BeanName[] emptyBeanNames = {
            new BeanName((String)null, true),
            new BeanName("", true),
        };
        
        BeanName ref = new BeanName("one.two.three", true);
        
        List<BeanName> candidates = new ArrayList<BeanName>();
        
        assertNull(ref.findBestMatch(candidates));

        for (BeanName bn : emptyBeanNames) {
            candidates.add(bn);
            assertNull(ref.findBestMatch(candidates));
        }  
        candidates.clear();
        
        for (BeanName bn : matchingBeanNames) {
            candidates.add(bn);
            assertTrue("no match with " + bn.getNormalisedName(), 
                       bn == ref.findBestMatch(candidates));
            candidates.clear();
        }
        
        for (BeanName bn : nonMatchingBeanNames) {
            candidates.add(bn);
            assertNull("match with " + bn.getNormalisedName(), 
                       ref.findBestMatch(candidates));
            candidates.clear();
        }
        
        for (BeanName bn : matchingBeanNames) {
            candidates.add(bn);            
        }
        assertTrue(matchingBeanNames[0] == ref.findBestMatch(candidates));
        
        for (BeanName bn : nonMatchingBeanNames) {
            candidates.add(0, bn);
        }
        assertTrue(matchingBeanNames[0] == ref.findBestMatch(candidates));
        candidates.clear();
        
    }
    
    public void testBestMatch() {
           
        BeanName ref = new BeanName("a.b");
        
        BeanName[] beanNames = {
            new BeanName("?.b"),
            new BeanName("*b"),
        };
        

        BeanName bestMatch;
        List<BeanName> candidates = new ArrayList<BeanName>();
        
        candidates.add(beanNames[0]);
        candidates.add(beanNames[1]);
        bestMatch = ref.findBestMatch(candidates);
        assertTrue(bestMatch.getName(), beanNames[0] == bestMatch);
        candidates.clear();
        
        candidates.add(beanNames[1]);
        candidates.add(beanNames[0]);
        bestMatch = ref.findBestMatch(candidates);
        assertTrue(bestMatch.getName(), beanNames[0] == bestMatch);
        candidates.clear();
        
        ref = new BeanName("a.b.c");
        
        beanNames = new BeanName[] {
            new BeanName("*c"),
            new BeanName("?.?.c"),
            new BeanName("a*c"),
            new BeanName("a.?.c")      
        };
        
        for (int i = 0; i < beanNames.length; i++) {
            for (int j = i + 1; j < beanNames.length; j++) {
                candidates.add(beanNames[i]);
                candidates.add(beanNames[j]);
                bestMatch = ref.findBestMatch(candidates);
                assertTrue("i = " + i + ", j = " + j, beanNames[j] == bestMatch);
                candidates.clear();
                candidates.add(beanNames[j]);
                candidates.add(beanNames[i]);
                bestMatch = ref.findBestMatch(candidates);
                assertTrue("i = " + i + ", j = " + j,  beanNames[j] == bestMatch);
                candidates.clear();        
            }
        }
        
        for (int i = 0; i < beanNames.length; i++) {
            candidates.add(beanNames[i]);
        }
        bestMatch = ref.findBestMatch(candidates);
        assertTrue(bestMatch.getName(), beanNames[beanNames.length - 1] == bestMatch);
        candidates.clear();
        
        for (int i = beanNames.length - 1; i >= 0; i--) {
            candidates.add(beanNames[i]);
        }
        bestMatch = ref.findBestMatch(candidates);
        assertTrue(bestMatch.getName(), beanNames[beanNames.length - 1] == bestMatch);
        candidates.clear();
    }
    
    public void testBestMatchWithEscapedBindingChars() {
        String busId = "celtix";
        String serviceName = "{http://www.objectweb.org/handlers}AddNumbersService";
        String portName = "addNumbersPort";
        
        BeanName ref = new BeanName(busId + "." + serviceName + "." + portName);
        
        BeanName[] beanNames = {
            new BeanName("*" + portName),
            new BeanName("?.?." + portName),
            new BeanName("?." + serviceName + "." + portName),
            new BeanName(busId + "*" + portName),
            new BeanName(busId + ".?." + portName),
        };
        
        List<BeanName> candidates = new ArrayList<BeanName>();
        for (BeanName bn : beanNames) {
            candidates.add(bn);
        }
        BeanName bestMatch = ref.findBestMatch(candidates);
        assertTrue(bestMatch.getName(), beanNames[beanNames.length - 1] == bestMatch);        
    }
    
    public void testBug304382() {

        BeanName ref = new BeanName("a.b.c");
        BeanName[] beanNames = {
            new BeanName("a.b"),
        };
        
        List<BeanName> candidates = new ArrayList<BeanName>();
        for (BeanName bn : beanNames) {
            candidates.add(bn);
        }
        BeanName bestMatch = ref.findBestMatch(candidates);
        assertNull(bestMatch);
    }
    
    public void testLastComponentAny() {
        BeanName ref = new BeanName("a.b.c");
        BeanName[] beanNames = {
            new BeanName("a.b.?"),
            new BeanName("a.b.?"),
            new BeanName("a.?.?"),
            new BeanName("?.?.?")
            
        };
        
        List<BeanName> candidates = new ArrayList<BeanName>();
        
        for (BeanName bn : beanNames) {
            candidates.add(bn);
            BeanName match = ref.findBestMatch(candidates);
            assertTrue(match == bn);
            candidates.clear();
        }
        
        for (BeanName bn : beanNames) {
            candidates.add(bn);
        }
        BeanName match = ref.findBestMatch(candidates);
        assertTrue(beanNames[0] == match);        
    }
}
