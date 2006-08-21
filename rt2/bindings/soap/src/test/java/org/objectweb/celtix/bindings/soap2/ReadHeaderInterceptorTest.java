package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.w3c.dom.Element;

import org.objectweb.celtix.bindings.attachments.AttachmentImpl;
import org.objectweb.celtix.bindings.attachments.AttachmentUtil;
import org.objectweb.celtix.interceptors.StaxInInterceptor;
import org.objectweb.celtix.message.Attachment;

public class ReadHeaderInterceptorTest extends TestBase {

    private ReadHeadersInterceptor rhi;
    private StaxInInterceptor staxIntc = new StaxInInterceptor();

    public void setUp() throws Exception {
        super.setUp();

        rhi = new ReadHeadersInterceptor();
        rhi.setPhase("phase1");
        chain.add(rhi);
    }

    public void testHandleHeader() {
        try {
            prepareSoapMessage("test-soap-header.xml");
        } catch (IOException ioe) {
            fail("Failed in creating soap message");
        }

        staxIntc.handleMessage(soapMessage);
        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        Element eleHeaders = soapMessage.getHeaders(Element.class);
        List headerChilds = new ArrayList<Element>();
        for (int i = 0; i < eleHeaders.getChildNodes().getLength(); i++) {
            if (eleHeaders.getChildNodes().item(i) instanceof Element) {
                Element element = (Element)eleHeaders.getChildNodes().item(i);
                headerChilds.add(element);
            }
        }
        assertEquals(2, headerChilds.size());
        for (int i = 0; i < headerChilds.size(); i++) {
            Element ele = (Element)headerChilds.get(i);
            if (ele.getLocalName().equals("reservation")) {
                Element reservation = ele;
                List reservationChilds = new ArrayList<Element>();
                for (int j = 0; j < reservation.getChildNodes().getLength(); j++) {
                    if (reservation.getChildNodes().item(j) instanceof Element) {
                        Element element = (Element)reservation.getChildNodes().item(j);
                        reservationChilds.add(element);
                    }
                }
                assertEquals(2, reservationChilds.size());
                assertEquals("reference", ((Element)reservationChilds.get(0)).getLocalName());
                assertEquals("uuid:093a2da1-q345-739r-ba5d-pqff98fe8j7d", ((Element)reservationChilds.get(0))
                    .getTextContent());
                assertEquals("dateAndTime", ((Element)reservationChilds.get(1)).getLocalName());
                assertEquals("2001-11-29T13:20:00.000-05:00", ((Element)reservationChilds.get(1))
                    .getTextContent());

            }
            if (ele.getLocalName().equals("passenger")) {
                Element passenger = ele;
                assertNotNull(passenger);
                Element child = null;
                for (int j = 0; j < passenger.getChildNodes().getLength(); j++) {
                    if (passenger.getChildNodes().item(j) instanceof Element) {
                        child = (Element)passenger.getChildNodes().item(j);
                    }
                }
                assertNotNull("passenger should has child element", child);                
                assertEquals("name", child.getLocalName());
                assertEquals("Bob", child.getTextContent());
            }

        }
    }

    private void prepareSoapMessage(String message) throws IOException {

        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass().getResourceAsStream(message),
                                                           "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setContent(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setContent(InputStream.class, bads.getInputStream());

    }
}
