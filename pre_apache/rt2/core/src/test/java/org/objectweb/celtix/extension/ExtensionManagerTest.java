package org.objectweb.celtix.extension;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.objectweb.celtix.resource.DefaultResourceManager;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;
import org.objectweb.celtix.resource.SinglePropertyResolver;

public class ExtensionManagerTest extends TestCase {

    private static final String EXTENSIONMANAGER_TEST_RESOURECE_NAME = "extensionManagerTest";
    private ExtensionManagerImpl manager;
    private MyService myService;
    private Map<Class, Object> extensions;
    
    public  void setUp() {
        ResourceResolver resolver = new SinglePropertyResolver(EXTENSIONMANAGER_TEST_RESOURECE_NAME, this);
        ResourceManager rm = new DefaultResourceManager(resolver);
        
        extensions = new HashMap<Class, Object>();
        extensions.put(Integer.class, new Integer(0));
        
        manager = new ExtensionManagerImpl("test-extension.xml", 
            Thread.currentThread().getContextClassLoader(), extensions, rm); 
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
        assertNull(myService);
        manager.activateViaNS(ns);
        assertNotNull(myService);
        assertEquals(1, myService.getActivationNamespaces().size());
        assertEquals(ns, myService.getActivationNamespaces().iterator().next());
        
        // second activation should be a no-op
        
        MyService first = myService;
        manager.activateViaNS(ns);
        assertSame(first, myService);
    }
    
    public void setMyService(MyService m) {
        myService = m;
    }

    
}
