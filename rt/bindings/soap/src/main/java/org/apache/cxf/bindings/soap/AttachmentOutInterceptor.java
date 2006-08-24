package org.apache.cxf.bindings.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.ResourceBundle;

import org.apache.cxf.bindings.attachments.CachedOutputStream;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptors.Fault;
import org.apache.cxf.jaxb.attachments.AttachmentSerializer;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.messaging.AbstractCachedOutputStream;
import org.apache.cxf.phase.Phase;

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
