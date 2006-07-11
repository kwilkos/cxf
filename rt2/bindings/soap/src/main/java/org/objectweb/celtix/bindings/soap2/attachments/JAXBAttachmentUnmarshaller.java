package org.objectweb.celtix.bindings.soap2.attachments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.message.Attachment;

public class JAXBAttachmentUnmarshaller extends AttachmentUnmarshaller {

    private SoapMessage message;

    public JAXBAttachmentUnmarshaller(SoapMessage messageParam) {
        super();
        this.message = messageParam;
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
        return AttachmentUtil.isXOPPackage(this.message);
    }
}
