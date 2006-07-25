package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.bindings.attachments.AttachmentImpl;
import org.objectweb.celtix.bindings.attachments.AttachmentUtil;
import org.objectweb.celtix.message.Attachment;


public class MustUnderstandInterceptorTest extends TestBase {

    public static final QName RESERVATION = new QName("http://travelcompany.example.org/reservation",
                                                      "reservation");

    public static final QName PASSENGER = new QName("http://mycompany.example.com/employees", "passenger");

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

        Exception ie = (Exception)soapMessage.getSource(Exception.class);
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

        Exception ie = (Exception)soapMessage.getSource(Exception.class);
        if (ie == null) {
            fail("InBound Exception Missing! Exception should be Can't understands QNames: " + PASSENGER
                 + ", ");
        } else {
            assertEquals("Exception should be Can't understands QNames: " + PASSENGER + ", ",
                         "Can't understands QNames: " + PASSENGER + ", ", ie.getMessage());
        }
    }

    private void prepareSoapMessage() throws IOException {

        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream("test-soap-header.xml"), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setSource(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setSource(InputStream.class, bads.getInputStream());

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
                    fail(e.toString());
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
