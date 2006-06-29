package org.objectweb.celtix.bindings.soap2.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;

import junit.framework.TestCase;

import org.objectweb.celtix.rio.Attachment;
import org.objectweb.celtix.rio.message.AbstractWrappedMessage;
import org.objectweb.celtix.rio.message.MessageImpl;
import org.objectweb.celtix.rio.phase.Phase;
import org.objectweb.celtix.rio.phase.PhaseInterceptorChain;
import org.objectweb.celtix.rio.soap.Soap11;
import org.objectweb.celtix.rio.soap.Soap12;
import org.objectweb.celtix.rio.soap.SoapMessage;
import org.objectweb.celtix.rio.soap.SoapVersion;

public class MultipartMessageInterceptorTest extends TestCase {

    private PhaseInterceptorChain chain;
    private MultipartMessageInterceptor mmi;

    public void setUp() throws Exception {
        List<Phase> phases = new ArrayList<Phase>();
        Phase phase1 = new Phase("phase1", 1);
        phases.add(phase1);
        chain = new PhaseInterceptorChain(phases);
        mmi = new MultipartMessageInterceptor();
        mmi.setPhase("phase1");
        chain.add(mmi);
    }

    public void testDoInterceptOfSoap12() {
        testDoIntercept(new Soap12());
    }

    public void testDoInterceptOfSoap11() {
        testDoIntercept(new Soap11());
    }

    private void testDoIntercept(SoapVersion soapVersion) {
        try {
            SoapMessage soapMessage = createSoapMessage(soapVersion);
            
            CachedOutputStream cos = new CachedOutputStream(64 * 1024, null);
            String contentType = AttachmentUtil.serializeMultipartMessage(soapMessage, cos);
            soapMessage.getAttachments().clear();
            
            assertTrue(cos.getInputStream() != null);
            soapMessage.setSource(InputStream.class, cos.getInputStream());
                        
            Map<String, String> mimeHttpHeaders = new HashMap<String, String>();
            soapMessage.put(AbstractWrappedMessage.MIME_HTTP_HEADERS, mimeHttpHeaders);
            mimeHttpHeaders.put("Content-Type", contentType);
            mimeHttpHeaders.put("Content-Description", "XML document Multi-Media attachment");

            chain.doIntercept(soapMessage);
            
            Attachment primarySoapPart = (Attachment) soapMessage.getSource(Attachment.class);
            assertTrue(primarySoapPart.getDataHandler() != null);
            
            Collection<Attachment> attachments = soapMessage.getAttachments();
            assertTrue(attachments.size() == 2);
            Iterator<Attachment> it = attachments.iterator();
            Attachment att1 = it.next();
            assertTrue(att1.getId().equals("cid:http://celtix.objectweb.org/me.jpg"));
            assertTrue(att1.getDataHandler() != null);
            
            Attachment att2 = it.next();
            assertTrue(att2.getId().equals("cid:http://celtix.objectweb.org/my.wav"));
            assertTrue(att2.getDataHandler() != null);
            
        } catch (IOException ioe) {
            fail(ioe.getStackTrace().toString());
        } catch (MessagingException me) {
            fail(me.getStackTrace().toString());
        }
    }

    private SoapMessage createSoapMessage(SoapVersion soapVersion) throws IOException {

        MessageImpl messageImpl = new MessageImpl();
        messageImpl.setInterceptorChain(chain);
        SoapMessage soapMessage = new SoapMessage(messageImpl);
        soapMessage.setVersion(soapVersion);

        // setup the message result with attachment.class
        ByteArrayDataSource bads = new ByteArrayDataSource(this.getClass()
            .getResourceAsStream("primarySoapPart.xml"), "Application/xop+xml");
        String cid = AttachmentUtil.createContentID("http://celtix.objectweb.org");
        soapMessage.setResult(Attachment.class, new AttachmentImpl(cid, new DataHandler(bads)));

        // setup the message attachments
        Collection<Attachment> attachments = soapMessage.getAttachments();
        String cidAtt1 = "cid:http://celtix.objectweb.org/me.jpg";
        bads = new ByteArrayDataSource(this.getClass().getResourceAsStream("me.jpg"), "image/jpg");
        AttachmentImpl att1 = new AttachmentImpl(cidAtt1, new DataHandler(bads));        
        att1.setXOP(true);
        attachments.add(att1);
        String cidAtt2 = "cid:http://celtix.objectweb.org/my.wav";
        bads = new ByteArrayDataSource(this.getClass().getResourceAsStream("my.wav"),
                                       "Application/octet-stream");
        AttachmentImpl att2 = new AttachmentImpl(cidAtt2, new DataHandler(bads));
        att2.setXOP(true);
        attachments.add(att2);

        return soapMessage;
    }
}
