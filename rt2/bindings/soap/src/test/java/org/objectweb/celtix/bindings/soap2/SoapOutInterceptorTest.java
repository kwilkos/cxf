package org.objectweb.celtix.bindings.soap2;

import java.io.*;
import java.util.*;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;


import org.objectweb.celtix.bindings.soap2.attachments.AttachmentImpl;
import org.objectweb.celtix.bindings.soap2.attachments.AttachmentUtil;
import org.objectweb.celtix.bindings.soap2.attachments.CachedOutputStream;
import org.objectweb.celtix.message.Attachment;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.staxutils.StaxUtils;


public class SoapOutInterceptorTest extends TestBase {
    private ReadHeadersInterceptor rhi;
    private SoapOutInterceptor soi;

    public void setUp() throws Exception {
        super.setUp();
        rhi = new ReadHeadersInterceptor();
        rhi.setPhase("phase1");
        chain.add(rhi);

        soi = new SoapOutInterceptor();
        soi.setPhase("phase2");
        chain.add(soi);
    }

    public void testHandleMessage() {
        try {
            prepareSoapMessage();
        } catch (IOException ioe) {
            fail("Failed in creating soap message! " + ioe.getMessage());
        }
        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        Exception oe = (Exception)soapMessage.get(Message.OUTBOUND_EXCEPTION);
        if (oe != null) {
            fail("OutBound Exception found! e=" + oe.getMessage());
        }
        CachedOutputStream cos = (CachedOutputStream)soapMessage.getResult(OutputStream.class);

        try {
            XMLStreamReader xmlReader = StaxUtils.createXMLStreamReader(cos.getInputStream());
            assertInputStream(xmlReader);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private void assertInputStream(XMLStreamReader xmlReader) throws Exception {

        int eventType = -1;
        boolean foundEnvelopeStart = false;
        boolean foundHeaderStart = false;
        boolean foundEnvelopeEnd = false;
        boolean foundHeaderEnd = false;
        
        while (xmlReader.hasNext()) {

            eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());                
                assertEquals("Envelop Start Element: ", name, Soap12.getInstance().getEnvelope());
                foundEnvelopeStart = true;
                break;
            }
        }
        while (xmlReader.hasNext()) {
            eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
                assertEquals("Header Start Element: ", name, Soap12.getInstance().getHeader());
                foundHeaderStart = true;
                break;
            }
        }
        while (xmlReader.hasNext()) {
            // Skip the contents of soap headers
            eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.END_ELEMENT
                && "passenger".equals(xmlReader.getLocalName())) {
                break;
            }
        }
        while (xmlReader.hasNext()) {
            eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
                assertEquals("Header End Element: ", name, Soap12.getInstance().getHeader());
                foundHeaderEnd = true;
                break;
            }
        }
        while (xmlReader.hasNext()) {
            eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                QName name = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
                assertEquals("Envelop End Element: ", name, Soap12.getInstance().getEnvelope());
                foundEnvelopeEnd = true;
                break;
            }
        }
        assertEquals("Evenlop Start Element founded first!", foundEnvelopeStart, true);
        assertEquals("Header Start Element founded first!", foundHeaderStart, true);
        assertEquals("Header End Element founded first!", foundHeaderEnd, true);
        assertEquals("Evenlop End Element founded first!", foundEnvelopeEnd, true);
    }

    private void prepareSoapMessage() throws IOException {

        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);

        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream("test-soap-header.xml"), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setSource(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));
        soapMessage.setSource(InputStream.class, bads.getInputStream());

        soapMessage.setResult(OutputStream.class, new CachedOutputStream(-1, null));
    }

}
