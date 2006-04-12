package org.objectweb.celtix.routing;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;

public class RouterManagerTest extends TestCase {
    private Map<String, Object> properties;
    private String javaClasspath;
    private File opDir;
    
    public void setUp() {
        properties = new HashMap<String, Object>();
        URL routerConfigFileUrl = getClass().getResource("resources/router_config1.xml");
        System.setProperty("celtix.config.file", routerConfigFileUrl.toString());
        javaClasspath = System.getProperty("java.class.path");
        
        opDir = new File(getClass().getResource(".").getFile(), "/temp");        
    }

    public void tearDown() throws Exception {
        System.setProperty("java.class.path", javaClasspath);
        RouteTypeUtil.deleteDir(opDir);
        
        Bus bus = Bus.getCurrent();
        bus.shutdown(true);
        Bus.setCurrent(null);
        
        System.clearProperty("celtix.config.file");
    }
    
    public void testGetRouterWSDLList() throws Exception {
        
        properties.put("org.objectweb.celtix.BusId", "celtix1");
        Bus bus = Bus.init(null, properties);
        
        RouterManager rm = new RouterManager(bus);
        List<String> urlList = rm.getRouteWSDLList();
        
        assertNotNull("a valid list should be present", urlList);
        assertEquals(1, urlList.size());
    }

    public void testInit() throws Exception {
        /*
        properties.put("org.objectweb.celtix.BusId", "celtix2");
        Bus bus = Bus.init(null, properties);
        
        TestRouterManager rm = new TestRouterManager(bus);
        rm.init();

        assertNotNull("Router Factory should be intialized", rm.getRouterFactory());
        
        List<Router> rList = rm.getRouters();
        assertNotNull("Router List should be initialized", rList);
        assertEquals(4, rList.size());
        
        //Calling of init creates a celtix-router-temp dir for the generated code
        RouteTypeUtil.deleteDir(new File(System.getProperty("user.dir"), "/celtix-router-tmp"));
        */
    }

    public void testInvokeWSDLToJava() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "celtix2");
        Bus bus = Bus.init(null, properties);

        TestRouterManager rm = new TestRouterManager(bus);

        //maven doesn't set java.class.path while eclipse does.
        boolean isClassPathSet = javaClasspath != null 
                                  && (javaClasspath.indexOf("JAXWS") >= 0);
        if (!isClassPathSet) {
            System.setProperty("java.class.path", getClassPath());
        }        

        File classDir = new File(opDir, "/classes");
        classDir.mkdirs();
        
        rm.testInvokeWSDLToJava(opDir, classDir);
        
        URLClassLoader loader = 
            URLClassLoader.newInstance(new URL[] {classDir.toURL()},
                                       null);
        
        Class<?> clz = loader.loadClass("org.objectweb.header_test.TestHeader");
        assertNotNull("TestHeader class instance should be present", clz);
        
        clz = loader.loadClass("org.objectweb.header_test.types.ObjectFactory");
        assertNotNull("ObjectFactory class instance should be present", clz);
        
        clz = loader.loadClass("org.objectweb.hwrouter.types.FaultDetail");
        assertNotNull("FaultDetail class instance should be present", clz);
        
        try {
            clz = loader.loadClass("org.objectweb.hwrouter.types.NotPresent");
            fail("Should throw a ClassNotFoundException");
        } catch (ClassNotFoundException cnfe) {
            //Expecetd Exception
        }
    }
    
    protected String getClassPath() {
        ClassLoader loader = getClass().getClassLoader();
        StringBuffer classPath = new StringBuffer();
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlLoader = (URLClassLoader)loader;
            for (URL url : urlLoader.getURLs()) {
                String file = url.getFile();
                if (file.indexOf("junit") == -1) {
                    classPath.append(url.getFile());
                    classPath.append(System.getProperty("path.separator"));
                }
            }
        }
        return classPath.toString();
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RouterManagerTest.class);
    }
    
    class TestRouterManager extends RouterManager {
        public TestRouterManager(Bus bus) {
            super(bus);
        }

        protected void publishRoutes() {
            //Complete
        }
        
        protected void invokeWSDLToJava(File srcDir, File classDir) {
            //Complete
        }
        
        public void testInvokeWSDLToJava(File srcDir, File classDir) {
            super.invokeWSDLToJava(srcDir, classDir);
        }
    }
    
}
