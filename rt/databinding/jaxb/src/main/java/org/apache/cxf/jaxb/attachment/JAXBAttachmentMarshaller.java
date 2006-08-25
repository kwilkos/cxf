package org.apache.cxf.jaxb.attachment;

import java.util.Collection;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

import org.apache.cxf.binding.attachment.AttachmentImpl;
import org.apache.cxf.binding.attachment.AttachmentUtil;
import org.apache.cxf.binding.attachment.ByteDataSource;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public class JAXBAttachmentMarshaller extends AttachmentMarshaller {

    private Message message;
    private Collection<Attachment> atts;
    private boolean isXop;

    public JAXBAttachmentMarshaller(Message messageParam) {
        super();
        this.message = messageParam;
        atts = message.getAttachments();
    }

    public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNS,
                                    String elementLocalName) {
        ByteDataSource source = new ByteDataSource(data, offset, length);
        source.setContentType(mimeType);
        DataHandler handler = new DataHandler(source);

        String id = AttachmentUtil.createContentID(elementNS);
        Attachment att = new AttachmentImpl(id, handler);
        atts.add(att);

        return "cid:" + id;
    }

    @Override
    public String addMtomAttachment(DataHandler handler, String elementNS, String elementLocalName) {

        String id = AttachmentUtil.createContentID(elementNS);
        Attachment att = new AttachmentImpl(id, handler);
        atts.add(att);

        return "cid:" + id;
    }

    @Override
    public String addSwaRefAttachment(DataHandler handler) {
        String id = UUID.randomUUID() + "@" + handler.getName();
        Attachment att = new AttachmentImpl(id, handler);
        atts.add(att);

        return id;
    }

    public void setXOPPackage(boolean xop) {
        this.isXop = xop;
    }

    @Override
    public boolean isXOPPackage() {
        return isXop;
    }
}
