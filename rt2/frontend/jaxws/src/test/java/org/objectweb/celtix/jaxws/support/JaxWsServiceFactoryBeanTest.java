package org.objectweb.celtix.jaxws.support;

import java.net.URL;

import junit.framework.TestCase;

import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

public class JaxWsServiceFactoryBeanTest extends TestCase {
    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setServiceClass(AnnotatedGreeterImpl.class);


        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);

        // CeltixBus bus = new CeltixBus();
        // bus.initialize(null);
        // bean.setBus(bus);
        
        // This doesn't work quite yet... Need to get the SOAP binding working
        // and the CeltixBus more amenable to unit testing.
        // Service service = bean.create();
        //         
        // assertEquals("SOAPService", service.getName().getLocalPart());
        // assertEquals("http://objectweb.org/hello_world_soap_http",
        // service.getName().getNamespaceURI());
    }
}
