package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.soap2.attachments.AttachmentImpl;
import org.objectweb.celtix.bindings.soap2.attachments.AttachmentUtil;
import org.objectweb.celtix.bindings.soap2.attachments.TestMimeUtil;
import org.objectweb.celtix.rio.Attachment;
import org.objectweb.celtix.rio.phase.Phase;
import org.objectweb.celtix.rio.phase.PhaseInterceptorChain;
import org.objectweb.celtix.rio.soap.Soap12;
import org.objectweb.celtix.rio.soap.SoapMessage;

public class ReadHeaderInterceptorTest extends TestCase {

    private PhaseInterceptorChain chain;
    private SoapMessage soapMessage;
    private ReadHeadersInterceptor rhi;

    public void setUp() throws Exception {
        List<Phase> phases = new ArrayList<Phase>();
        Phase phase1 = new Phase("phase1", 1);
        phases.add(phase1);
        chain = new PhaseInterceptorChain(phases);
        rhi = new ReadHeadersInterceptor();
        rhi.setPhase("phase1");
        chain.add(rhi);
    }

    public void tearDown() {

    }

    public void testDoIntercept() {
        try {
            prepareSoapMessage();
        } catch (IOException ioe) {
            fail("Failed in creating soap message");
        }
        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertTrue(soapMessage.getHeaders().size() == 2);
        Iterator it = soapMessage.getHeaders().keySet().iterator();
        while (it.hasNext()) {
            QName qname = (QName)it.next();
            if (qname.getLocalPart().equals("reservation")) {
                Element reservation = soapMessage.getHeaders().get(qname);
                assertTrue(reservation.getChildNodes().getLength() == 2);
                assertTrue(reservation.getChildNodes().item(0).getLocalName().equals("reference"));
                assertTrue(reservation.getChildNodes().item(0).getTextContent()
                    .equals("uuid:093a2da1-q345-739r-ba5d-pqff98fe8j7d"));
                assertTrue(reservation.getChildNodes().item(1).getLocalName().equals("dateAndTime"));
                assertTrue(reservation.getChildNodes().item(1).getTextContent()
                    .equals("2001-11-29T13:20:00.000-05:00"));

            }
            if (qname.getLocalPart().equals("passenger")) {
                Element passenger = soapMessage.getHeaders().get(qname);
                assertTrue(passenger.getChildNodes().getLength() == 1);
                assertTrue(passenger.getChildNodes().item(0).getLocalName().equals("name"));
                assertTrue(passenger.getChildNodes().item(0).getTextContent().equals("Åke Jógvan Øyvind"));
            }

        }
    }

    private void prepareSoapMessage() throws IOException {

        soapMessage = TestMimeUtil.createEmptySoapMessage(new Soap12(), chain);
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream("test-soap-header.xml"), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setSource(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setSource(InputStream.class, bads.getInputStream());

    }
}
