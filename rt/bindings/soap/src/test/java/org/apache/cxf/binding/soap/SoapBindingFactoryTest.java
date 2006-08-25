package org.apache.cxf.binding.soap;

import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.BindingFactoryManagerImpl;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.easymock.IMocksControl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class SoapBindingFactoryTest extends TestCase {

    public void testFactory() throws Exception {
        Definition d = createDefinition();

        IMocksControl control = createNiceControl();
        Bus bus = control.createMock(Bus.class);

        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        BindingFactoryManager bfm = new BindingFactoryManagerImpl();
        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bfm);
        control.replay();

        WSDLServiceBuilder builder = new WSDLServiceBuilder(bus);
        ServiceInfo serviceInfo = builder
            .buildService(d, new QName("http://apache.org/hello_world_soap_http", "SOAPService"));

        BindingInfo bi = serviceInfo.getBindings().iterator().next();

        assertTrue(bi instanceof SoapBindingInfo);

        SoapBindingInfo sbi = (SoapBindingInfo)bi;
        assertEquals("document", sbi.getStyle());
        assertEquals("http://schemas.xmlsoap.org/soap/http", sbi.getTransportURI());

        BindingOperationInfo boi = sbi.getOperation(new QName("http://apache.org/hello_world_soap_http",
                                                              "sayHi"));
        SoapOperationInfo sboi = boi.getExtensor(SoapOperationInfo.class);
        assertNotNull(sboi);
        assertEquals("document", sboi.getStyle());
        assertEquals("", sboi.getAction());

        BindingMessageInfo input = boi.getInput();
        SoapBodyInfo bodyInfo = input.getExtensor(SoapBodyInfo.class);
        assertEquals("literal", bodyInfo.getUse());

        List<MessagePartInfo> parts = bodyInfo.getParts();
        assertNotNull(parts);
        assertEquals(1, parts.size());
    }

    public Definition createDefinition() throws Exception {
        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        return WSDLFactory.newInstance().newWSDLReader().readWSDL("hello_world.wsdl",
                                                                  new InputSource(resource.openStream()));
    }
}
