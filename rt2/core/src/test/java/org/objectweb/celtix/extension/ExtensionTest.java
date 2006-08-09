package org.objectweb.celtix.extension;

import junit.framework.TestCase;

public class ExtensionTest extends TestCase {

    public void testMutators() {
        Extension e = new Extension();
        String namespaceURI = "http://schemas.xmlsoap.org/wsdl/soap/";
        e.setName(namespaceURI);
        assertEquals("Unexpected namespaceURI.", namespaceURI, e.getName());
        String className = "org.objectweb.celtix.bindings.soap.SoapBinding";
        e.setClassName(className);
        assertEquals("Unexpected classname.", className, e.getClassName());
        assertTrue("Extension is deferred.", !e.isDeferred());
        e.setDeferred(true);
        assertTrue("Extension is not deferred.", e.isDeferred());
        assertEquals("Unexpected size of namespace list.", 0, e.getNamespaces().size());
    }
    
    public void testLoad() throws ExtensionException {
        Extension e = new Extension();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        e.setClassName("no.such.Extension");        
        try {
            e.load(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap ClassNotFoundException",
                       ex.getCause() instanceof ClassNotFoundException);
        }

        e.setClassName("java.lang.System");
        try {
            e.load(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap IllegalAccessException",
                       ex.getCause() instanceof IllegalAccessException);
        } 
        e.setClassName(MyServiceConstructorThrowsException.class.getName());
        try {
            e.load(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap InstantiationException",
                       ex.getCause() instanceof InstantiationException);
        } 
        e.setClassName("java.lang.String");
        Object obj = e.load(cl);
        assertTrue("Object is not type String", obj instanceof String);        
    }
    
    class MyServiceConstructorThrowsException {
        public MyServiceConstructorThrowsException() {
            throw new IllegalArgumentException();
        }
    }
    
}
