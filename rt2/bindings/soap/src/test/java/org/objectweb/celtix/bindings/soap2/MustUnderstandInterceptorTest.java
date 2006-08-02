package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.attachments.AttachmentImpl;
import org.objectweb.celtix.bindings.attachments.AttachmentUtil;
import org.objectweb.celtix.message.Attachment;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.wsdl11.ServiceWSDLBuilder;
import org.objectweb.celtix.wsdl11.WSDLServiceBuilder;

public class MustUnderstandInterceptorTest extends TestBase {

    private static final QName RESERVATION = new QName("http://travelcompany.example.org/reservation",
                                                       "reservation");
    private static final QName PASSENGER = new QName("http://mycompany.example.com/employees", "passenger");

    private MustUnderstandInterceptor mui;
    private DummySoapInterceptor dsi;
    private ReadHeadersInterceptor rhi;

    private Definition def;
    private Service service;

    private WSDLServiceBuilder wsdlServiceBuilder;
    private ServiceInfo serviceInfo;

    private IMocksControl control;
    private Bus bus;
    private BindingFactoryManager bindingFactoryManager;

    public void setUp() throws Exception {

        super.setUp();

        rhi = new ReadHeadersInterceptor();
        rhi.setPhase("phase1");
        chain.add(rhi);

        mui = new MustUnderstandInterceptor();
        mui.setPhase("phase2");
        chain.add(mui);

        dsi = new DummySoapInterceptor();
        dsi.setPhase("phase3");
        chain.add(dsi);
    }

    public void testHandleMessageSucc() {
        try {
            prepareSoapMessage();
            dsi.getUnderstoodHeaders().add(RESERVATION);
            dsi.getUnderstoodHeaders().add(PASSENGER);
        } catch (IOException ioe) {
            fail("Failed in creating soap message");
        }
        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertEquals("HeaderInterceptor run correctly!", 2, soapMessage.getHeaders(Element.class)
            .getChildNodes().getLength());
        assertEquals("DummaySoapInterceptor getRoles has been called!", true, dsi.isCalledGetRoles());
        assertEquals("DummaySoapInterceptor getUnderstood has been called!", true, dsi
            .isCalledGetUnderstood());

        Exception ie = (Exception)soapMessage.getContent(Exception.class);
        if (ie != null) {
            fail("InBound Exception found! e=" + ie.getMessage());
        }
    }

    public void testHandleMessageFail() {
        try {
            prepareSoapMessage();
            dsi.getUnderstoodHeaders().add(RESERVATION);
        } catch (IOException ioe) {
            fail("Failed in creating soap message");
        }
        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertEquals("DummaySoapInterceptor getRoles has been called!", true, dsi.isCalledGetRoles());
        assertEquals("DummaySoapInterceptor getUnderstood has been called!", true, dsi
            .isCalledGetUnderstood());

        Exception ie = (Exception)soapMessage.getContent(Exception.class);
        if (ie == null) {
            fail("InBound Exception Missing! Exception should be Can't understands QNames: " + PASSENGER
                 + ", ");
        } else {
            assertEquals("Exception should be Can't understands QNames: " + PASSENGER + ", ",
                         "Can't understands QNames: " + PASSENGER + ", ", ie.getMessage());
        }
    }

    public void testHandleMessageWithHeaderParam() {
        try {
            prepareSoapMessage();
            dsi.getUnderstoodHeaders().add(RESERVATION);
            mockServiceModel();
            soapMessage.put(Message.BINDING_INFO, serviceInfo
                .getBinding(new QName("http://mycompany.example.com/employees", "headerTesterSOAPBinding")));
            soapMessage.put(Message.OPERATION_INFO, "inHeader");
        } catch (IOException ioe) {
            fail("Failed in creating soap message");
        } catch (Exception wse) {
            fail("Failed in mocking wsdl service model");
        }

        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertEquals("HeaderInterceptor run correctly!", 2, soapMessage.getHeaders(Element.class)
            .getChildNodes().getLength());
        assertEquals("DummaySoapInterceptor getRoles has been called!", true, dsi.isCalledGetRoles());
        assertEquals("DummaySoapInterceptor getUnderstood has been called!", true, dsi
            .isCalledGetUnderstood());

        Exception ie = (Exception)soapMessage.getContent(Exception.class);
        if (ie != null) {
            fail("InBound Exception found! e=" + ie.getMessage());
        }
    }

    private void prepareSoapMessage() throws IOException {

        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream("test-soap-header.xml"), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setContent(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setContent(InputStream.class, bads.getInputStream());

    }

    private void mockServiceModel() throws Exception {

        String wsdlUrl = this.getClass().getResource("test-soap-header.wsdl").toString();
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        def = wsdlReader.readWSDL(wsdlUrl);

        control = EasyMock.createNiceControl();                
        bus = control.createMock(Bus.class);
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        wsdlServiceBuilder = new WSDLServiceBuilder(bus);

        for (Iterator it = def.getServices().values().iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof Service) {
                service = (Service)obj;
                break;
            }
        }

        EasyMock.expect(bus.getBindingManager()).andReturn(bindingFactoryManager);
        control.replay();

        serviceInfo = wsdlServiceBuilder.buildService(def, service);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_DEFINITION, null);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_SERVICE, null);
        ServiceWSDLBuilder.getServiceWSDLBuilder().buildDefinition(serviceInfo);
    }

    private class DummySoapInterceptor extends AbstractSoapInterceptor {

        private boolean calledGetRoles;
        private boolean calledGetUnderstood;

        private Set<URI> roles = new HashSet<URI>();
        private Set<QName> understood = new HashSet<QName>();

        public void handleMessage(SoapMessage messageParam) {
        }

        public Set<URI> getRoles() {
            calledGetRoles = true;
            if (roles.size() == 0) {
                try {
                    roles.add(new URI("http://www.w3.org/2003/05/soap-envelope/role/next"));
                    roles.add(new URI("http://www.w3.org/2003/05/soap-envelope/role/none"));
                    roles.add(new URI("http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
                } catch (Exception e) {
                    return null;
                }
            }
            return roles;
        }

        public Set<QName> getUnderstoodHeaders() {
            calledGetUnderstood = true;
            return understood;
        }

        public boolean isCalledGetRoles() {
            return calledGetRoles;
        }

        public boolean isCalledGetUnderstood() {
            return calledGetUnderstood;
        }

    }

}
