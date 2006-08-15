package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.InputStream;

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
        assertEquals(2, eleHeaders.getChildNodes().getLength());
        for (int i = 0; i < eleHeaders.getChildNodes().getLength(); i++) {
            Element ele = (Element)eleHeaders.getChildNodes().item(i);            
            if (ele.getLocalName().equals("reservation")) {
                Element reservation = ele;                
                assertEquals(2, reservation.getChildNodes().getLength());
                assertEquals("reference", reservation.getChildNodes().item(0).getLocalName());
                assertEquals("uuid:093a2da1-q345-739r-ba5d-pqff98fe8j7d", reservation.getChildNodes().item(0)
                    .getTextContent());
                assertEquals("dateAndTime", reservation.getChildNodes().item(1).getLocalName());
                assertEquals("2001-11-29T13:20:00.000-05:00", reservation.getChildNodes().item(1)
                    .getTextContent());

            }
            if (ele.getLocalName().equals("passenger")) {
                Element passenger = ele;
                assertNotNull(passenger);                
                assertEquals(1, passenger.getChildNodes().getLength());
                assertEquals("name", passenger.getChildNodes().item(0).getLocalName());
                assertEquals("Bob", passenger.getChildNodes().item(0).getTextContent());
            }

        }
    }

    private void prepareSoapMessage(String message) throws IOException {

        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream(message), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setContent(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setContent(InputStream.class, bads.getInputStream());

    }
}
