package org.objectweb.celtix.routing;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
//import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.hello_world_doc_lit.Greeter;

public class SEIImplHandlerTest extends TestCase {

    private Map<String, Object> properties;
    public void setUp() {
        properties = new HashMap<String, Object>();
    }

    public void tearDown() throws Exception {
        Bus bus = Bus.getCurrent();
        bus.shutdown(true);
        Bus.setCurrent(null);
    }

    public void testServiceCreation() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "MPT1");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);

        URL wsdlUrl = getClass().getResource("resources/router.wsdl");
        Definition def = bus.getWSDLManager().getDefinition(wsdlUrl);

        QName sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPXMLServiceSource");
        String sourcePort = new String("HTTPXMLPortSource");
        QName destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        String destPort = new String("HTTPSoapPortDestination");

        RouteType rt =
            RouteTypeUtil.createRouteType("normal_route",
                                           sourceSrv, sourcePort,
                                           destSrv, destPort);
        
        TestHandler th = new TestHandler(def, rt);
        List<Object> proxyList = th.doInit(Greeter.class);
        
        assertNotNull("List of Client Proxies should notbe null", proxyList);
        assertEquals("Should have one client proxy", 1 , proxyList.size());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SEIImplHandlerTest.class);
    }

    class TestHandler extends SEIImplHandler {
        public TestHandler(Definition model, RouteType rt) {
            super(model, rt);
        }
        
        public List<Object> doInit(Class<?> seiClass) {
            super.init(seiClass);
            return super.proxyList;
        }
    }
}
