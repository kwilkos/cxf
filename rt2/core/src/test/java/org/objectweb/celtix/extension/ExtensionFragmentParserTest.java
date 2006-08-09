package org.objectweb.celtix.extension;

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
        assertEquals("Unexpected name.", "immediate", e.getName());
        assertEquals("Unexpected class.", "java.lang.String", e.getClassName());
        Collection<String> namespaces = e.getNamespaces();
        for (String ns : namespaces) {
            assertTrue("Unexpected namespace.", "http://celtix.objectweb.org/a/b/c".equals(ns)
                                                || "http://celtix.objectweb.org/d/e/f".equals(ns));
        }
        assertEquals("Unexpected number of namespace elements.", 2, namespaces.size());
        
        e = extensions.get(1);
        assertTrue("Extension is not deferred.", e.isDeferred());
        assertEquals("Unexpected name.", "deferredBoolean", e.getName());
        assertEquals("Unexpected class.", "java.lang.Boolean", e.getClassName());
        namespaces = e.getNamespaces();
        for (String ns : namespaces) {
            assertEquals("Unexpected namespace.", "http://celtix.objectweb.org/x/y/z", ns);            
        }
        assertEquals("Unexpected number of namespace elements.", 1, namespaces.size());
    }
}
