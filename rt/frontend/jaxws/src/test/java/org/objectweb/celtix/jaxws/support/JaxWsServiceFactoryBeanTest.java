package org.apache.cxf.jaxws.support;

import java.lang.reflect.Method;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.apache.cxf.Bus;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.bindings.BindingFactoryManagerImpl;
import org.apache.cxf.bindings.soap2.SoapBindingFactory;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.SimpleMethodInvoker;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.apache.hello_world_soap_http.GreeterImpl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class JaxWsServiceFactoryBeanTest extends TestCase {
    public void testEndpoint() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setServiceClass(GreeterImpl.class);

        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);

        Bus bus = createBus();
        bean.setBus(bus);

        SimpleMethodInvoker invoker = new SimpleMethodInvoker(new GreeterImpl());
        bean.setInvoker(invoker);
        
        Service service = bean.create();

        assertEquals("SOAPService", service.getName().getLocalPart());
        assertEquals("http://apache.org/hello_world_soap_http", service.getName().getNamespaceURI());
        
        InterfaceInfo intf = service.getServiceInfo().getInterface();
        
        OperationInfo op = intf.getOperation(
            new QName("http://apache.org/hello_world_soap_http", "sayHi"));
        
        Method m = (Method) op.getProperty(Method.class.getName());
        assertNotNull(m);
        assertEquals("sayHi", m.getName());
        
        assertEquals(invoker, service.getInvoker());
    }

    Bus createBus() throws Exception {
        IMocksControl control = createNiceControl();
        Bus bus = control.createMock(Bus.class);

        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        BindingFactoryManager bfm = new BindingFactoryManagerImpl();
        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bfm).anyTimes();

        WSDLManagerImpl wsdlMan = new WSDLManagerImpl();
        expect(bus.getExtension(WSDLManager.class)).andReturn(wsdlMan);

        control.replay();

        return bus;
    }
}
