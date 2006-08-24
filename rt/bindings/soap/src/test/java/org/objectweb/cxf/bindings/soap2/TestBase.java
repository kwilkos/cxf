package org.apache.cxf.bindings.soap2;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class TestBase extends TestCase {

    protected PhaseInterceptorChain chain;
    protected SoapMessage soapMessage;

    public void setUp() throws Exception {
        List<Phase> phases = new ArrayList<Phase>();
        Phase phase1 = new Phase("phase1", 1);
        Phase phase2 = new Phase("phase2", 2);
        Phase phase3 = new Phase("phase3", 3);
        phases.add(phase1);
        phases.add(phase2);
        phases.add(phase3);
        chain = new PhaseInterceptorChain(phases);

        soapMessage = TestUtil.createEmptySoapMessage(new Soap11(), chain);
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

    public ServiceInfo getTestService(Class<?> clz) {
        // FIXME?!?!?!?? There should NOT be JAX-WS stuff here
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
