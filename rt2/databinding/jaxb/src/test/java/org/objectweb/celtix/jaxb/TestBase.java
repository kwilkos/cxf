package org.objectweb.celtix.jaxb;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.MessageImpl;

import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.staxutils.StaxUtils;
import org.objectweb.celtix.wsdl11.WSDLServiceBuilder;

public class TestBase extends TestCase {

    protected PhaseInterceptorChain chain;
    protected MessageImpl message;
    
    public void setUp() throws Exception {
        message = new MessageImpl();
        Exchange exchange = new ExchangeImpl();
        message.setExchange(exchange);
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

    protected BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        ServiceInfo service = getMockedServiceModel(getClass().getResource(wsdlUrl).toString());
        assertNotNull(service);
        BindingInfo binding = service.getEndpoint(new QName(service.getName().getNamespaceURI(), port))
            .getBinding();
        assertNotNull(binding);
        return binding;
    }

    protected ServiceInfo getMockedServiceModel(String wsdlUrl) throws Exception {
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        Definition def = wsdlReader.readWSDL(wsdlUrl);

        IMocksControl control = EasyMock.createNiceControl();
        Bus bus = control.createMock(Bus.class);
        BindingFactoryManager bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);

        Service service = null;
        for (Iterator<?> it = def.getServices().values().iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof Service) {
                service = (Service)obj;
                break;
            }
        }

        EasyMock.expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bindingFactoryManager);
        control.replay();

        ServiceInfo serviceInfo = wsdlServiceBuilder.buildService(def, service);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_DEFINITION, null);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_SERVICE, null);
        return serviceInfo;
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
