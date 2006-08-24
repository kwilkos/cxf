package org.objectweb.celtix.bindings.soap2;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.bindings.attachments.AttachmentImpl;
import org.objectweb.celtix.bindings.attachments.AttachmentUtil;
import org.objectweb.celtix.message.Attachment;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public class MustUnderstandInterceptorTest extends TestBase {

    private static final QName RESERVATION = new QName("http://travelcompany.example.org/reservation",
                                                       "reservation");
    private static final QName PASSENGER = new QName("http://mycompany.example.com/employees", "passenger");

    private MustUnderstandInterceptor mui;
    private DummySoapInterceptor dsi;
    private ReadHeadersInterceptor rhi;

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

    public void testHandleMessageSucc() throws Exception {
        prepareSoapMessage();
        dsi.getUnderstoodHeaders().add(RESERVATION);
        dsi.getUnderstoodHeaders().add(PASSENGER);

        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertEquals("DummaySoapInterceptor getRoles has been called!", true, dsi.isCalledGetRoles());
        assertEquals("DummaySoapInterceptor getUnderstood has been called!", true, dsi
            .isCalledGetUnderstood());
    }

    public void testHandleMessageFail() throws Exception {
        prepareSoapMessage();

        dsi.getUnderstoodHeaders().add(RESERVATION);

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        assertEquals("DummaySoapInterceptor getRoles has been called!", true, dsi.isCalledGetRoles());
        assertEquals("DummaySoapInterceptor getUnderstood has been called!", true, dsi
            .isCalledGetUnderstood());

        SoapFault ie = (SoapFault)soapMessage.getContent(Exception.class);
        if (ie == null) {
            fail("InBound Exception Missing! Exception should be Can't understands QNames: " + PASSENGER);
        } else {
            assertEquals(SoapFault.MUST_UNDERSTAND, ie.getFaultCode());
            assertEquals("Can not understand QNames: " + PASSENGER, ie.getMessage().toString());
        }
    }

    public void testHandleMessageWithHeaderParam() throws Exception {
        prepareSoapMessage();
        dsi.getUnderstoodHeaders().add(RESERVATION);
        ServiceInfo serviceInfo = getMockedServiceModel(getClass().getResource("test-soap-header.wsdl")
            .toString());

        BindingInfo binding = serviceInfo.getBinding(new QName("http://org.objectweb.celtix/headers",
                                                               "headerTesterSOAPBinding"));
        BindingOperationInfo bop = binding.getOperation(new QName("http://org.objectweb.celtix/headers",
                                                                  "inHeader"));
        soapMessage.getExchange().put(BindingOperationInfo.class, bop);

        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertEquals("DummaySoapInterceptor getRoles has been called!", true, dsi.isCalledGetRoles());
        assertEquals("DummaySoapInterceptor getUnderstood has been called!", true, dsi
            .isCalledGetUnderstood());
    }

    private void prepareSoapMessage() throws Exception {

        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream("test-soap-header.xml"), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setContent(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(bads.getInputStream()));

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
