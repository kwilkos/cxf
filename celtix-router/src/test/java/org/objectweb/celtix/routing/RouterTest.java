package org.objectweb.celtix.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;

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
        properties.put("org.objectweb.celtix.BusId", "celtix1");
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

    private void testRouterSourceAndDestination(Definition def, 
                                              QName sourceSrv, String sourcePort, 
                                              QName destSrv, String destPort,
                                              boolean isSameBinding) {
        RouteType rt = createRouteType("route_0", 
                                       sourceSrv, sourcePort, 
                                       destSrv, destPort);
        
        TestRouter router = new TestRouter(def, rt);
        assertNotNull("WSDL Model should be set for the router", router.getWSDLModel());
        assertNotNull("RouteType should be set for the router", router.getRoute());

        List<Service> s = router.getDestinationServices();
        assertEquals(1, s.size());
        assertEquals(destSrv, s.get(0).getQName());
        
        Port p = router.getDestinationPorts(s.get(0));
        assertNotNull("Should have a wsdl port", p);
        assertEquals(destPort, p.getName());
        
        s = router.getSourceServices();
        assertEquals(1, s.size());
        assertEquals(sourceSrv, s.get(0).getQName());
        p = router.getSourcePort(s.get(0));
        assertNotNull("Should have a wsdl port", p);
        assertEquals(sourcePort, p.getName());
        
        //Check For Same Binding
        assertEquals(isSameBinding, 
                   router.testIsSameBindingId(p.getBinding()));        
    }
    
    private RouteType createRouteType(String routeName, 
                                      QName srcService, String srcPort, 
                                      QName destService, String destPort) {
        SourceType st = new SourceType();
        st.setService(srcService);
        st.setPort(srcPort);
        
        DestinationType dt = new DestinationType();
        dt.setPort(destPort);
        dt.setService(destService);
        
        RouteType rt = new RouteType();
        rt.setName(routeName);

        List<SourceType> sList = rt.getSource();
        sList.add(st);
        List<DestinationType> dList = rt.getDestination();
        dList.add(dt);
        
        return rt;
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RouterTest.class);
    }


    class TestRouter extends Router {

        public TestRouter(Definition model, RouteType rt) {
            super(model, rt);
        }

        public boolean testIsSameBindingId(Binding binding) {
            return super.isSameBindingId(binding);
        }
        
        public List<Service> getSourceServices() {
            return super.sourceServices;
        }
        
        public List<Service> getDestinationServices() {
            return super.destServices;
        }
        
        public Port getSourcePort(Service service) {
            return (Port) super.sourcePortMap.get(service);
        }
        
        public Port getDestinationPorts(Service service) {
            return (Port) super.destPortMap.get(service);
        }
        
    }
}
