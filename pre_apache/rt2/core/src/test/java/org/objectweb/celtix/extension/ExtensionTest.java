package org.objectweb.celtix.extension;

import junit.framework.Test;
import junit.framework.TestCase;

public class ExtensionTest extends TestCase {

    public void testMutators() {
        Extension e = new Extension();
        
        String className = "org.objectweb.celtix.bindings.soap.SoapBinding";
        e.setClassname(className);
        assertEquals("Unexpected class name.", className, e.getClassname());
        assertNull("Unexpected interface name.", e.getInterfaceName());
        
        String interfaceName = "org.objectweb.celtix.bindings.Binding";
        e.setInterfaceName(interfaceName);
        assertEquals("Unexpected interface name.", interfaceName, e.getInterfaceName());
        
        assertTrue("Extension is deferred.", !e.isDeferred());
        e.setDeferred(true);
        assertTrue("Extension is not deferred.", e.isDeferred());
        
        assertEquals("Unexpected size of namespace list.", 0, e.getNamespaces().size());
    }
    
    public void testLoad() throws ExtensionException {
        Extension e = new Extension();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        e.setClassname("no.such.Extension");        
        try {
            e.load(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap ClassNotFoundException",
                       ex.getCause() instanceof ClassNotFoundException);
        }

        e.setClassname("java.lang.System");
        try {
            e.load(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap IllegalAccessException",
                       ex.getCause() instanceof IllegalAccessException);
        } 
        e.setClassname(MyServiceConstructorThrowsException.class.getName());
        try {
            e.load(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap InstantiationException",
                       ex.getCause() instanceof InstantiationException);
        } 
        e.setClassname("java.lang.String");
        Object obj = e.load(cl);
        assertTrue("Object is not type String", obj instanceof String);        
    }
    
    public void testLoadInterface() {
        Extension e = new Extension();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        e.setInterfaceName("no.such.Extension");        
        try {
            e.loadInterface(cl);                  
        } catch (ExtensionException ex) {
            assertTrue("ExtensionException does not wrap ClassNotFoundException",
                       ex.getCause() instanceof ClassNotFoundException);
        }
        
        e.setInterfaceName(Test.class.getName());
        Class<?> cls = e.loadInterface(cl);
        assertTrue("Object is not type Class", cls instanceof Class); 
    }
    
    class MyServiceConstructorThrowsException {
        public MyServiceConstructorThrowsException() {
            throw new IllegalArgumentException();
        }
    }
    
}
