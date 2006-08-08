package org.objectweb.celtix.jaxws.support;

import java.net.URL;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.BindingFactoryManagerImpl;
import org.objectweb.celtix.bindings.soap2.SoapBindingFactory;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.wsdl11.WSDLManagerImpl;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class JaxWsServiceFactoryBeanTest extends TestCase {
    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setServiceClass(AnnotatedGreeterImpl.class);

        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);

        Bus bus = createBus();
        bean.setBus(bus);

        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http", service.getName().getNamespaceURI());
        
        
    }

    Bus createBus() throws Exception {
        IMocksControl control = createNiceControl();
        Bus bus = control.createMock(Bus.class);

        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        BindingFactoryManager bfm = new BindingFactoryManagerImpl();
        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        expect(bus.getBindingManager()).andReturn(bfm).anyTimes();

        WSDLManagerImpl wsdlMan = new WSDLManagerImpl();
        expect(bus.getWSDL11Manager()).andReturn(wsdlMan);

        control.replay();

        return bus;
    }
}
