package org.objectweb.celtix.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;

public class ExtensionManagerTest extends TestCase {

    private ExtensionManagerImpl manager;
    private IMocksControl control;
    private MyService myService;
    private Map<Class, Object> extensions;
    
    public  void setUp() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("extensionManagerTest", this);
        extensions = new HashMap<Class, Object>();
        extensions.put(Integer.class, new Integer(0));
        manager = new ExtensionManagerImpl("test-extension.xml", 
            Thread.currentThread().getContextClassLoader(), extensions, properties); 
        MyService.instances.clear();
        myService = null;
    }
    
    public void testLoadAndRegister() {
        Extension e = new Extension();
        e.setClassname("java.lang.String");
        e.setDeferred(false);        
        manager.loadAndRegister(e);
        
        
        String interfaceName = "java.lang.Runnable";
        e.setDeferred(false);
        e.setClassname("java.lang.Thread");
        e.setInterfaceName(interfaceName);
        assertNull("Object is registered.", extensions.get(Runnable.class));
        manager.loadAndRegister(e);
        assertNotNull("Object was not registered.", extensions.get(Runnable.class));
      
        interfaceName = "java.lang.Integer";
        e.setInterfaceName(interfaceName);
        e.setClassname("no.such.Class");
        Object obj = extensions.get(Integer.class);
        assertNotNull("Object is not registered.", obj);
        manager.loadAndRegister(e);
        assertSame("Registered object was replaced.", obj, extensions.get(Integer.class));
         
    }
    
    @SuppressWarnings("unchecked")
    public void testActivateViaNS() {        
        
        Extension e = new Extension();
        e.setClassname(MyService.class.getName());
        String ns = "http://celtix.objectweb.org/integer";
        e.getNamespaces().add(ns);
        e.setDeferred(true);
        manager.processExtension(e);
        manager.activateViaNS(ns);
        assertEquals("Unexpected number of MyService instances.", 1, MyService.instances.size());
        assertSame(this, MyService.instances.get(0).extensionManagerTest);
        assertSame(myService, MyService.instances.get(0));
    }
    
    public static class MyService {
        
        static List<MyService> instances = new ArrayList<MyService>();
        
        @Resource
        ExtensionManagerTest extensionManagerTest;
        
        MyService() {
            instances.add(this);
        }
        
        @PostConstruct
        void registerMyselfAsExtension() {
            extensionManagerTest.myService = this;
        }
    }

    
}
