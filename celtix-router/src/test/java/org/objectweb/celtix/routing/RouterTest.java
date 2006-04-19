package org.objectweb.celtix.routing;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.routing.configuration.RouteType;

public class RouterTest extends TestCase {

    private Map<String, Object> properties;
    private String javaClasspath;
    private File srcDir;
    
    public void setUp() {
        properties = new HashMap<String, Object>();
        javaClasspath = System.getProperty("java.class.path");
        srcDir = new File(getClass().getResource(".").getFile(), "/temp");    
    }

    public void tearDown() throws Exception {
        System.setProperty("java.class.path", javaClasspath);
        RouteTypeUtil.deleteDir(srcDir);
        
        Bus bus = Bus.getCurrent();
        bus.shutdown(true);
        Bus.setCurrent(null);
    }

    public void testRouterCreation() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "RT1");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);

        Definition def = bus.getWSDLManager().getDefinition(getClass().getResource("resources/router.wsdl"));

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

    public void testPassThroughRouterInit() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "RT2");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);

        Definition def = bus.getWSDLManager().getDefinition(getClass().getResource("resources/router.wsdl"));

        QName sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceSource");
        String sourcePort = new String("HTTPSoapPortSource");
        QName destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        String destPort = new String("HTTPSoapPortDestination");

        RouteType rt = 
            RouteTypeUtil.createRouteType("route_0", 
                                           sourceSrv, sourcePort, 
                                           destSrv, destPort);
        
        TestRouter router = new TestRouter(null, def, rt);
        router.init();
        assertEquals(1, router.epList.size());
        Endpoint ep = router.epList.get(0);
        assertNotNull("Should have a Endpoint for Source Service", ep);
        assertNotNull("Should have a wsdl model", ep.getMetadata());
        Map<String, Object> props = ep.getProperties();
        assertNotNull("Should have a wsdl model", props);
        assertEquals(sourceSrv,  props.get(Endpoint.WSDL_SERVICE));
        QName portName = (QName) props.get(Endpoint.WSDL_PORT);
        assertEquals(sourceSrv.getNamespaceURI(), portName.getNamespaceURI());
        assertEquals(sourcePort, portName.getLocalPart());
        Object impl = ep.getImplementor();
        assertTrue("Should be instance of Provider<Source>", 
                   impl instanceof Provider);
        StreamSourceMessageProvider ssmp = (StreamSourceMessageProvider) impl;
        assertNull("WebServiceContext is not set as endpoint is not published",
                   ssmp.getContext());
    }

    public void testNormalRouterInit() throws Exception {
        properties.put("org.objectweb.celtix.BusId", "RT3");
        Bus bus = Bus.init(null, properties);
        Bus.setCurrent(bus);

        URL wsdlURl = getClass().getResource("resources/router.wsdl");
        Definition def = bus.getWSDLManager().getDefinition(wsdlURl);

        QName sourceSrv = new QName("http://objectweb.org/HWRouter", "HTTPXMLServiceSource");
        String sourcePort = new String("HTTPXMLPortSource");
        QName destSrv = new QName("http://objectweb.org/HWRouter", "HTTPSoapServiceDestination");
        String destPort = new String("HTTPSoapPortDestination");

        RouteType rt = 
            RouteTypeUtil.createRouteType("route_1", 
                                           sourceSrv, sourcePort, 
                                           destSrv, destPort);

        ClassLoader loader = new URLClassLoader(new URL[] {srcDir.toURL()}, null);
        //Test with no code generation.
        TestRouter router = new TestRouter(loader, def, rt);
        try {
            router.init();
            fail("Should throw a WebServiceException with cause of ClassNotFoundError");
        } catch (WebServiceException ex) {
            if  (ex.getCause() instanceof ClassNotFoundException) {
                //Expected
            }
        }

        //Test With CodeGeneration and URLClassLoadere
        loader = doCodeGeneration(wsdlURl.getFile(), srcDir);
        
        router = new TestRouter(loader, def, rt);
        router.init();
        assertEquals(1, router.epList.size());
        Endpoint ep = router.epList.get(0);
        assertNotNull("Should have a Endpoint for Source Service", ep);
        assertNotNull("Should have a wsdl model", ep.getMetadata());
        Map<String, Object> props = ep.getProperties();
        assertNotNull("Should have a wsdl model", props);
        assertEquals(sourceSrv,  props.get(Endpoint.WSDL_SERVICE));
        QName portName = (QName) props.get(Endpoint.WSDL_PORT);
        assertEquals(sourceSrv.getNamespaceURI(), portName.getNamespaceURI());
        assertEquals(sourcePort, portName.getLocalPart());
        Object impl = ep.getImplementor();
        
        //The Implementor Should be a proxy class.
        assertTrue("Implemetor Should be a proxy Class", 
                     Proxy.isProxyClass(impl.getClass()));

        InvocationHandler implHandler = Proxy.getInvocationHandler(impl);
        assertTrue("Invocation Handler should be instance of SEIImplHandler",
                   implHandler instanceof SEIImplHandler);
        SEIImplHandler seiHandler = (SEIImplHandler)implHandler;
        assertNull("Should have a WebServiceContext set",
                   seiHandler.getContext());
    }

    private ClassLoader doCodeGeneration(String wsdlUrl, File opDir) throws Exception {
        //maven doesn't set java.class.path while eclipse does.
        boolean isClassPathSet = javaClasspath != null 
                                  && (javaClasspath.indexOf("JAXWS") >= 0);
        if (!isClassPathSet) {
            System.setProperty("java.class.path", 
                               RouteTypeUtil.getClassPath(getClass().getClassLoader()));
        }

        File classDir = new File(opDir, "/classes");
        classDir.mkdirs();
        
        RouteTypeUtil.invokeWSDLToJava(wsdlUrl, opDir, classDir);

        URLClassLoader loader = 
            URLClassLoader.newInstance(new URL[] {classDir.toURL()},
                                       getClass().getClassLoader());
        return loader;
    }

    private void testRouterSourceAndDestination(Definition def, 
                                              QName sourceSrv, String sourcePort, 
                                              QName destSrv, String destPort,
                                              boolean isSameBinding) {
        RouteType rt = 
            RouteTypeUtil.createRouteType("route_0", 
                                           sourceSrv, sourcePort, 
                                           destSrv, destPort);
        
        TestRouter router = new TestRouter(null, def, rt);
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


    class TestRouter extends RouterImpl {

        public TestRouter(ClassLoader loader, Definition model, RouteType rt) {
            super(loader, model, rt);
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
