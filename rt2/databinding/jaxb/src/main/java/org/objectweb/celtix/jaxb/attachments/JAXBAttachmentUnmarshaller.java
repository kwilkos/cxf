package org.objectweb.celtix.jaxb.attachments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.message.Attachment;
import org.objectweb.celtix.message.Message;

public class JAXBAttachmentUnmarshaller extends AttachmentUnmarshaller {

    private Message message;

    public JAXBAttachmentUnmarshaller(Message messageParam) {
        super();
        this.message = messageParam;
        AttachmentDeserializer ad = (AttachmentDeserializer)message.get(Message.ATTACHMENT_DESERIALIZER);
        if (ad == null) {
            throw new WebServiceException("Can't find Attachment Deserializer in message"
                                          + " when doing JAXBAttachmentUnmarshaller");
        } else {
            try {
                ad.processAttachments();
            } catch (Exception e) {
                throw new WebServiceException(e);
            }
        }
    }

    @Override
    public DataHandler getAttachmentAsDataHandler(String contentId) {
        // TODO Auto-generated method stub
        for (Attachment a : message.getAttachments()) {
            if (contentId.equals(a.getId())) {
                return a.getDataHandler();
            }
        }
        throw new IllegalArgumentException("Attachment " + contentId + " was not found.");
    }

    @Override
    public byte[] getAttachmentAsByteArray(String contentId) {
        // TODO Auto-generated method stub
        Attachment att = null;
        for (Attachment a : message.getAttachments()) {
            if (contentId.equals(a.getId())) {
                att = a;
            }
        }
        if (att == null) {
            throw new IllegalArgumentException("Attachment " + contentId + " was not found.");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            copy(att.getDataHandler().getInputStream(), bos);
        } catch (IOException e) {
            throw new WebServiceException("Could not read attachment.", e);
        }
        return bos.toByteArray();
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        try {
            final byte[] buffer = new byte[8096];
            for (int n = input.read(buffer); -1 != n; n = input.read(buffer)) {
                output.write(buffer, 0, n);
            }
        } finally {
            output.close();
            input.close();
        }
    }

    @Override
    public boolean isXOPPackage() {
        String contentTypeOfSoapBodyPart;
        String typeOfSoapBodyPart;
        Attachment primaryMimePart = message.getContent(Attachment.class);
        if (primaryMimePart == null) {
            return false;
        } else {
            contentTypeOfSoapBodyPart = primaryMimePart.getHeader("Content-Type");
        }
        if ("application/xop+xml".equals(contentTypeOfSoapBodyPart)) {
            typeOfSoapBodyPart = primaryMimePart.getHeader("type");
            if (typeOfSoapBodyPart.indexOf("application/soap+xml") >= 0) {
                return true;
            } else if (typeOfSoapBodyPart.indexOf("text/xml") >= 0) {
                return true;
            }
        }
        return false;

    }

}
