package org.apache.cxf.jaxb;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bindings.Binding;
import org.apache.cxf.bindings.BindingFactory;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.interceptors.WrappedInInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.apache.hello_world_soap_http.Greeter;

import org.easymock.classextension.IMocksControl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class TestBase extends TestCase {

    PhaseInterceptorChain chain;
    MessageImpl message;
    Bus bus;
    ServiceInfo serviceInfo;
    BindingInfo bindingInfo;
    Service service;
    EndpointInfo endpointInfo;
    EndpointImpl endpoint;
    BindingOperationInfo operation;

    public void setUp() throws Exception {
        bus = new CXFBusFactory().createBus();

        BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);

        IMocksControl control = createNiceControl();
        BindingFactory bf = control.createMock(BindingFactory.class);
        Binding binding = control.createMock(Binding.class);
        expect(bf.createBinding(null)).andStubReturn(binding);

        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bf);

        String ns = "http://apache.org/hello_world_soap_http";
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, getClass()
            .getResource("/org/apache/cxf/jaxb/resources/wsdl/hello_world.wsdl"),
                                                            new QName(ns, "SOAPService"));

        service = factory.create();
        endpointInfo = service.getServiceInfo().getEndpoint(new QName(ns, "SoapPort"));
        endpoint = new EndpointImpl(bus, service, endpointInfo);
        service.setDataReaderFactory(getTestReaderFactory(Greeter.class));
        service.setDataWriterFactory(getTestWriterFactory(Greeter.class));

        operation = endpointInfo.getBinding().getOperation(new QName(ns, "greetMe"));
        operation.getOperationInfo().setProperty(WrappedInInterceptor.SINGLE_WRAPPED_PART, Boolean.TRUE);

        message = new MessageImpl();
        Exchange exchange = new ExchangeImpl();
        message.setExchange(exchange);

        exchange.put(Service.class, service);
        exchange.put(Endpoint.class, endpoint);
        exchange.put(Binding.class, endpoint.getBinding());
    }

    public void tearDown() throws Exception {
    }

    public InputStream getTestStream(Class<?> clz, String file) {
        return clz.getResourceAsStream(file);
    }

    public XMLStreamReader getXMLStreamReader(InputStream is) {
        return StaxUtils.createXMLStreamReader(is);
    }

    public XMLStreamWriter getXMLStreamWriter(OutputStream os) {
        return StaxUtils.createXMLStreamWriter(os);
    }

    public Method getTestMethod(Class<?> sei, String methodName) {
        Method[] iMethods = sei.getMethods();
        for (Method m : iMethods) {
            if (methodName.equals(m.getName())) {
                return m;
            }
        }
        return null;
    }

    protected JAXBDataReaderFactory getTestReaderFactory(Class<?> clz) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        JAXBDataReaderFactory readerFacotry = new JAXBDataReaderFactory();
        readerFacotry.setJAXBContext(ctx);
        return readerFacotry;
    }

    protected JAXBDataWriterFactory getTestWriterFactory(Class<?> clz) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        JAXBDataWriterFactory writerFacotry = new JAXBDataWriterFactory();
        writerFacotry.setJAXBContext(ctx);
        return writerFacotry;
    }
}
