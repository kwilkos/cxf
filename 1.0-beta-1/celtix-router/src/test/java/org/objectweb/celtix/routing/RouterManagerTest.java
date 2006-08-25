package org.objectweb.celtix.routing;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.wsdl.WSDLManager;

public class RouterManagerTest extends TestCase {
    private Map<String, Object> properties;
    public void setUp() {
        properties = new HashMap<String, Object>();
        URL routerConfigFileUrl = getClass().getResource("resources/router_config1.xml");
        System.setProperty("celtix.config.file", routerConfigFileUrl.toString());        
    }

    public void tearDown() throws Exception {
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
        
        properties.put("org.objectweb.celtix.BusId", "celtix2");
        Bus bus = Bus.init(null, properties);
        
        TestRouterManager rm = new TestRouterManager(bus);
        rm.init();

        List<String> urlList = rm.getRouteWSDLList();
        WSDLManager wsdlManager = Bus.getCurrent().getWSDLManager();

        for (String wsdlUrl : urlList) {
            URL url = getClass().getResource(wsdlUrl);
            assertNotNull("Should have a valid wsdl definition", 
                          wsdlManager.getDefinition(url));
        }
        assertNotNull("Router Factory should be intialized", rm.getRouterFactory());
        
        List<Router> rList = rm.getRouters();
        assertNotNull("Router List should be initialized", rList);
        assertEquals(1, rList.size());
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
    }
    
}
