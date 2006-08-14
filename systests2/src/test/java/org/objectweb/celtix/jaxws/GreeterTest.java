package org.objectweb.celtix.jaxws;

import java.net.URL;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.CeltixBus;
import org.objectweb.celtix.jaxws.support.JaxWsServiceFactoryBean;
import org.objectweb.celtix.service.Service;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

public class GreeterTest extends TestCase {
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
        assertEquals("http://objectweb.org/hello_world_soap_http",
            service.getName().getNamespaceURI());
               
        bean.activateEndpoints();
    }

    Bus createBus() {
        return new CeltixBus();
    }
//    Bus createBus() throws Exception {
//        IMocksControl control = createNiceControl();
//        Bus bus = control.createMock(Bus.class);
//
//        SoapBindingFactory bindingFactory = new SoapBindingFactory();
//        BindingFactoryManager bfm = new BindingFactoryManagerImpl();
//        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);
//
//        expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bfm).anyTimes();
//
//        WSDLManagerImpl wsdlMan = new WSDLManagerImpl();
//        expect(bus.getExtension(WSDLManager.class)).andReturn(wsdlMan).anyTimes();
//
//        DestinationFactoryManagerImpl dfm = new DestinationFactoryManagerImpl();
//        expect(bus.getExtension(DestinationFactoryManager.class)).andReturn(dfm).anyTimes();
//        
//        HTTPTransportFactory httpTransFactory = new HTTPTransportFactory();
//        //httpTransFactory.setBus(bus);
//        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", httpTransFactory);
//        
//        SoapDestinationFactory soapDF = new SoapDestinationFactory(dfm);
//        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);
//
//        control.replay();
//
//        return bus;
//    }
}
