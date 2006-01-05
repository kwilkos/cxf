package org.objectweb.celtix.bus.configuration.spring;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.tools.generators.spring.SpringUtils;

public class SpringUtilsTest extends TestCase {

    public void testGetBeanClassName() {
        assertEquals("com.mycompany.a.b.c.myclass.spring.MyclassBean", SpringUtils
            .getBeanClassName("http://www.mycompany.com/a/b/c/MyClass"));
        
        assertEquals("myclass.spring.MyclassBean", SpringUtils
                     .getBeanClassName("MyClass"));
    }
    
    
    public void testStringToQName() {
        QName in = new QName(XMLConstants.XML_NS_URI, "abc");
        QName out = SpringUtils.stringToQName(in.toString());
        assertEquals(in.getNamespaceURI(), out.getNamespaceURI());
        assertEquals(in.getLocalPart(), out.getLocalPart());

        in = new QName(XMLConstants.NULL_NS_URI, "abc");  
        out = SpringUtils.stringToQName(in.toString());
        assertEquals(in.getNamespaceURI(), out.getNamespaceURI());
        assertEquals(in.getLocalPart(), out.getLocalPart());
        
        try {
            SpringUtils.stringToQName("{abc");
            fail("Expected IllegalArgumentException not thrown.");
        } catch (IllegalArgumentException ex) {
            // ignore
        }
        
        out = SpringUtils.stringToQName("{abc}");
        assertEquals("abc", out.getNamespaceURI());
        assertEquals("", out.getLocalPart());
    }
    
}
