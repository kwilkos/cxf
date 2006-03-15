package org.objectweb.celtix.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.routing.configuration.RouteType;

public class RouterFactoryTest extends TestCase {
    
    private Map<String, Object> properties;
    public void setUp() {
        properties = new HashMap<String, Object>();
    }

    public void tearDown() throws Exception {
        Bus bus = Bus.getCurrent();
        bus.shutdown(true);
        Bus.setCurrent(null);
    }
    
    public void testAddRoutes() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "celtix1");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);
        
        TestRouterFactory factory = new TestRouterFactory();
        factory.init(bus);
        
        List<Definition> modelList = new ArrayList<Definition>();
        Definition def = bus.getWSDLManager().getDefinition(getClass().getResource("/wsdl/router.wsdl"));
        modelList.add(def);
        
        factory.addRoutes(modelList);        
        assertEquals(1, factory.routerCount);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RouterFactoryTest.class);
    }
    
    
    class TestRouterFactory extends RouterFactory {
        private int routerCount;
        public TestRouterFactory() {
            super();
            routerCount = 0;
        }
        
        public Router createRouter(Definition model, RouteType route) {
            Router router = super.createRouter(model, route);
            ++routerCount;
            return router;
        }
    }
}
