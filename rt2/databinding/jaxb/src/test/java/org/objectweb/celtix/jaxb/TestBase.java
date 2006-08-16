package org.objectweb.celtix.jaxb;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bus.CeltixBus;
import org.objectweb.celtix.endpoint.EndpointImpl;
import org.objectweb.celtix.interceptors.WrappedInInterceptor;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.staxutils.StaxUtils;
import org.objectweb.celtix.wsdl11.WSDLServiceFactory;
import org.objectweb.hello_world_soap_http.Greeter;

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
        bus = new CeltixBus();

        BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);

        IMocksControl control = createNiceControl();
        BindingFactory bf = control.createMock(BindingFactory.class);
        Binding binding = control.createMock(Binding.class);
        expect(bf.createBinding(null)).andStubReturn(binding);

        bfm.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bf);

        String ns = "http://objectweb.org/hello_world_soap_http";
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, getClass()
            .getResource("/org/objectweb/celtix/jaxb/resources/wsdl/hello_world.wsdl"),
                                                            new QName(ns, "SOAPService"));

        service = factory.create();
        endpointInfo = service.getServiceInfo().getEndpoint(new QName(ns, "SoapPort"));
        endpoint = new EndpointImpl(bus, service, endpointInfo);
        service.setDataReaderFactory(getTestReaderFactory(Greeter.class));
        service.setDataWriterFactory(getTestWriterFactory(Greeter.class));

        operation = endpointInfo.getBinding().getOperation(new QName(ns, "greetMe"));
        operation.setProperty(WrappedInInterceptor.SINGLE_WRAPPED_PART, Boolean.TRUE);

        message = new MessageImpl();
        Exchange exchange = new ExchangeImpl();
        message.setExchange(exchange);

        exchange.put(ExchangeConstants.SERVICE, service);
        exchange.put(ExchangeConstants.ENDPOINT, endpoint);
        exchange.put(ExchangeConstants.BINDING, endpoint.getBinding());
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
