package org.objectweb.celtix.extension;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.resource.DefaultResourceManager;
import org.objectweb.celtix.resource.PropertiesResolver;

public class ExtensionManagerTest extends TestCase {

    private ExtensionManager manager;
    
    public  void setUp() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "cxf");
        ResourceInjector injector = new ResourceInjector(new DefaultResourceManager(
            new PropertiesResolver(properties)));
        manager = new ExtensionManager("test-extension.xml", 
            Thread.currentThread().getContextClassLoader(), injector); 
        MyService.counter = 0;
    }
    
    public void testProcessExtension() {
        Extension e = new Extension();
        e.setClassName("java.lang.String");
        String name = "immediate";
        e.setName(name);
        e.setDeferred(false);
        Object obj = manager.processExtension(e);
        assertNotNull("Object was not loaded.", obj);
        assertSame("Loaded object was not inserted in map of active objects.", obj, manager.get(name));
        e.setDeferred(true);
        e.setClassName("no.such.Class");
        String ns = "http://celtix.objectweb.org/deferred";
        e.getNamespaces().add(ns);
        obj = manager.processExtension(e);
        assertNull("Object was loaded.", obj);
    }
    
    public void testActivateViaNS() {
        Extension e = new Extension();
        e.setClassName(MyService.class.getName());
        String ns = "http://celtix.objectweb.org/integer";
        e.getNamespaces().add(ns);
        String name = "deferred";
        e.setName(name);
        e.setDeferred(true);
        manager.processExtension(e);
        manager.activateViaNS(ns);
        assertEquals("Unexpected number of MyService instances.", 1, MyService.counter);
        assertNotNull("Loaded object was not inserted in map of active objects.", manager.get(name));
        // second activation should be a no-op
        manager.activateViaNS("http://celtix.objectweb.org/integer");
        assertEquals("Unexpected number of MyService instances.", 1, MyService.counter);
    }
    
    public static class MyService {
        static int counter;
        
        MyService() {
            counter++;
        }
    }
    
    
}
