package org.objectweb.celtix.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.routing.configuration.RouteType;

public class RouterTest extends TestCase {

    private Map<String, Object> properties;
    public void setUp() {
        properties = new HashMap<String, Object>();
    }

    public void tearDown() throws Exception {
        Bus bus = Bus.getCurrent();
        bus.shutdown(true);
        Bus.setCurrent(null);
    }

    public void testRouterCreation() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "RT1");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);

        Definition def = bus.getWSDLManager().getDefinition(getClass().getResource("/wsdl/router.wsdl"));

        QName sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceSource");
        String sourcePort = new String("HTTPSoapPortSource");
        QName destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        String destPort = new String("HTTPSoapPortDestination");
        
        testRouterSourceAndDestination(def,
                                       sourceSrv, sourcePort,
                                       destSrv, destPort,
                                       true);
        
        sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPXMLServiceSource");
        sourcePort = new String("HTTPXMLPortSource");
        destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        destPort = new String("HTTPSoapPortDestination");
        
        testRouterSourceAndDestination(def,
                                       sourceSrv, sourcePort,
                                       destSrv, destPort,
                                       false);        
    }

    public void testRouterInit() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "RT2");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);

        Definition def = bus.getWSDLManager().getDefinition(getClass().getResource("/wsdl/router.wsdl"));

        QName sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceSource");
        String sourcePort = new String("HTTPSoapPortSource");
        QName destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        String destPort = new String("HTTPSoapPortDestination");

        RouteType rt = 
            RouteTypeUtil.createRouteType("route_0", 
                                           sourceSrv, sourcePort, 
                                           destSrv, destPort);
        
        TestRouter router = new TestRouter(def, rt);
        router.init();
        assertEquals(1, router.epList.size());
        Endpoint ep = router.epList.get(0);
        assertNotNull("Should have a Endpoint for Source Service", ep);
        assertNotNull("Should have a wsdl model", ep.getMetadata());
        Map<String, Object> props = ep.getProperties();
        assertNotNull("Should have a wsdl model", props);
        assertEquals(sourceSrv,  props.get(Endpoint.WSDL_SERVICE));
        assertEquals(sourcePort, props.get(Endpoint.WSDL_PORT));
        Object impl = ep.getImplementor();
        assertTrue("Should be instance of Provider<Source>", impl instanceof Provider);
    }
    
    private void testRouterSourceAndDestination(Definition def, 
                                              QName sourceSrv, String sourcePort, 
                                              QName destSrv, String destPort,
                                              boolean isSameBinding) {
        RouteType rt = 
            RouteTypeUtil.createRouteType("route_0", 
                                           sourceSrv, sourcePort, 
                                           destSrv, destPort);
        
        TestRouter router = new TestRouter(def, rt);
        assertNotNull("WSDL Model should be set for the router", router.getWSDLModel());
        assertNotNull("RouteType should be set for the router", router.getRoute());

        Service s = def.getService(destSrv);
        Port p = router.getDestinationPorts(s);
        
        assertNotNull("Should have a wsdl port", p);
        assertEquals(destPort, p.getName());
        
        s = def.getService(sourceSrv);
        p = router.getSourcePort(s);
        
        assertNotNull("Should have a wsdl port", p);
        assertEquals(sourcePort, p.getName());
        
        //Check For Same Binding
        assertEquals(isSameBinding, 
                   router.testIsSameBindingId(p));        
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RouterTest.class);
    }


    class TestRouter extends Router {

        public TestRouter(Definition model, RouteType rt) {
            super(model, rt);
        }

        public boolean testIsSameBindingId(Port p) {
            WsdlPortProvider provider = new WsdlPortProvider(p);
            return super.isSameBindingId((String) provider.getObject("bindingId"));
        }
        
        public Port getSourcePort(Service service) {
            return (Port) super.sourcePortMap.get(service.getQName());
        }
        
        public Port getDestinationPorts(Service service) {
            return (Port) super.destPortMap.get(service.getQName());
        }
        
        public List<Endpoint> getEndpoints() {
            return super.epList;
        }
    }
}
