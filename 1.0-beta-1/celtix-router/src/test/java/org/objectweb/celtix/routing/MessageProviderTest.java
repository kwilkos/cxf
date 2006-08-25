package org.objectweb.celtix.routing;

import java.net.URL;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.routing.configuration.RouteType;

public class MessageProviderTest extends TestCase {

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

        URL wsdlUrl = getClass().getResource("/wsdl/router.wsdl");
        Definition def = bus.getWSDLManager().getDefinition(wsdlUrl);

        QName sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceSource");
        String sourcePort = new String("HTTPSoapPortSource");
        QName destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        String destPort = new String("HTTPSoapPortDestination");

        RouteType rt = 
            RouteTypeUtil.createRouteType("route_0",
                                           sourceSrv, sourcePort,
                                           destSrv, destPort);
        TestProvider tp = new TestProvider(def, rt);

        Service s = tp.createService(wsdlUrl, destSrv);
        assertNotNull("Service should not be null", s);
        assertEquals(destSrv, s.getServiceName());
        assertEquals(wsdlUrl, s.getWSDLDocumentLocation());
        
        Dispatch<StreamSource> d = tp.createDispatch(s, destPort);
        assertNotNull("Dispatch instance should be present", d);
        
        tp.init();
        
        tp.testDispatchList(1);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MessageProviderTest.class);
    }
    
    
    class TestProvider extends StreamSourceMessageProvider {
        
        public TestProvider(Definition def, RouteType rt) {
            super(def, rt);
        }
        
        public void testDispatchList(int expectedCount) {
            assertEquals(expectedCount, this.dList.size());
        }
    }
}
