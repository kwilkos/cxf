package org.objectweb.celtix.bindings.soap2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;

import org.objectweb.celtix.interceptors.StaxInInterceptor;
import org.objectweb.celtix.staxutils.StaxUtils;

public class SoapOutInterceptorTest extends TestBase {
    private ReadHeadersInterceptor rhi;
    private SoapOutInterceptor soi;

    public void setUp() throws Exception {
        super.setUp();
        StaxInInterceptor sii = new StaxInInterceptor();
        sii.setPhase("phase1");
        chain.add(sii);

        rhi = new ReadHeadersInterceptor();
        rhi.setPhase("phase2");
        chain.add(rhi);

        soi = new SoapOutInterceptor();
        soi.setPhase("phase3");
        chain.add(soi);
    }

    public void testHandleMessage() throws Exception {
        prepareSoapMessage();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMessage.setContent(OutputStream.class, out);

        soapMessage.getInterceptorChain().doIntercept(soapMessage);
        assertNotNull(soapMessage.getHeaders(Element.class));

        Exception oe = (Exception)soapMessage.getContent(Exception.class);
        if (oe != null) {
            throw oe;
        }

        InputStream bis = new ByteArrayInputStream(out.toByteArray());
        XMLStreamReader xmlReader = StaxUtils.createXMLStreamReader(bis);
        assertInputStream(xmlReader);
    }

    private void assertInputStream(XMLStreamReader xmlReader) throws Exception {
        assertEquals(XMLStreamReader.START_ELEMENT, xmlReader.nextTag());
        assertEquals(Soap12.getInstance().getEnvelope(), xmlReader.getName());

        assertEquals(XMLStreamReader.START_ELEMENT, xmlReader.nextTag());
        assertEquals(Soap12.getInstance().getHeader(), xmlReader.getName());

        assertEquals(XMLStreamReader.START_ELEMENT, xmlReader.nextTag());
        assertEquals("reservation", xmlReader.getLocalName());

        assertEquals(XMLStreamReader.START_ELEMENT, xmlReader.nextTag());
        assertEquals("reference", xmlReader.getLocalName());
        // I don't think we're writing the body yet...
        //
        // assertEquals(XMLStreamReader.START_ELEMENT, xmlReader.nextTag());
        // assertEquals(Soap12.getInstance().getBody(), xmlReader.getName());
    }

    private void prepareSoapMessage() throws IOException {
        soapMessage = TestUtil.createEmptySoapMessage(new Soap12(), chain);

        soapMessage.setContent(InputStream.class, getClass().getResourceAsStream("test-soap-header.xml"));
    }

}
