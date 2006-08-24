package org.apache.cxf.extension;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

public class ExtensionFragmentParserTest extends TestCase {

    public void testGetExtensions() {
        InputStream is = ExtensionFragmentParserTest.class.getResourceAsStream("extension1.xml");
        List<Extension> extensions = new ExtensionFragmentParser().getExtensions(is);
        assertEquals("Unexpected number of Extension elements.", 3, extensions.size());
        
        Extension e = extensions.get(0);
        assertTrue("Extension is deferred.", !e.isDeferred());
        assertEquals("Unexpected class name.", 
                     "org.apache.cxf.foo.FooImpl", e.getClassname());
        Collection<String> namespaces = e.getNamespaces();
        for (String ns : namespaces) {
            assertTrue("Unexpected namespace.", "http://cxf.apache.org/a/b/c".equals(ns)
                                                || "http://cxf.apache.org/d/e/f".equals(ns));
        }
        assertEquals("Unexpected number of namespace elements.", 2, namespaces.size());
        
        e = extensions.get(1);
        assertTrue("Extension is not deferred.", e.isDeferred());
        assertEquals("Unexpected implementation class name.", 
                     "java.lang.Boolean", e.getClassname());
        namespaces = e.getNamespaces();
        for (String ns : namespaces) {
            assertEquals("Unexpected namespace.", "http://cxf.apache.org/x/y/z", ns);            
        }
        assertEquals("Unexpected number of namespace elements.", 1, namespaces.size());
    }
}
