package org.objectweb.celtix.bindings.soap2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.ResourceBundle;

import org.objectweb.celtix.bindings.attachments.CachedOutputStream;
import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.interceptors.Fault;
import org.objectweb.celtix.jaxb.attachments.AttachmentSerializer;
import org.objectweb.celtix.message.Attachment;
import org.objectweb.celtix.messaging.AbstractCachedOutputStream;
import org.objectweb.celtix.phase.Phase;

public class AttachmentOutInterceptor extends AbstractSoapInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SoapOutInterceptor.class);

    public AttachmentOutInterceptor() {
        super();
        setPhase(Phase.WRITE);
    }

    public void handleMessage(SoapMessage message) throws Fault {
        AbstractCachedOutputStream ops = (AbstractCachedOutputStream)message.getContent(OutputStream.class);
        try {
            Collection<Attachment> attachments = message.getAttachments();
            if (attachments.size() > 0) {
                CachedOutputStream cos = new CachedOutputStream();
                AttachmentSerializer as = new AttachmentSerializer(message, ops.getInputStream(), cos);
                as.serializeMultipartMessage();
                ops.resetOut(cos, false);
            }           
            ops.flush();
            ops.close();
        } catch (IOException ioe) {
            throw new SoapFault(new Message("ATTACHMENT_IO", BUNDLE, ioe.toString()), 
                                SoapFault.ATTACHMENT_IO);
        }
    }


}
