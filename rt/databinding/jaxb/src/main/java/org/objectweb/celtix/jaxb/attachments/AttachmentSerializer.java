package org.apache.cxf.jaxb.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.bindings.attachments.AttachmentDataSource;
import org.apache.cxf.bindings.attachments.AttachmentUtil;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

public class AttachmentSerializer {

    private static final String[] FILTER = new String[] {"Message-ID", "Mime-Version", "Content-Type"};

    private Message message;
    private InputStream in;
    private OutputStream out;

    public AttachmentSerializer(Message messageParam, InputStream inParam, OutputStream outParam) {
        message = messageParam;
        in = inParam;
        out = outParam;
    }

    /**
     * Using result in soapMessage & attachment to write to output stream
     * 
     * @param soapMessage
     * @param in input stream contain the attachment
     * @param out
     * @throws CxfRioException
     */

    public String serializeMultipartMessage() throws WebServiceException {

        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);
        String soapPartId = AttachmentUtil.createContentID(null);
        String subType = AttachmentUtil.getMimeSubType(message, soapPartId);
        MimeMultipart mimeMP = new MimeMultipart(subType);

        // InputStream in = soapMessage.getContent(InputStream.class);
        AttachmentDataSource ads = new AttachmentDataSource("application/xop+xml", in);
        MimeBodyPart soapPart = new MimeBodyPart();
        try {
            soapPart.setDataHandler(new DataHandler(ads));
            soapPart.setContentID("<" + soapPartId + ">");
            soapPart.addHeader("Content-Type", "application/xop+xml");
            soapPart.addHeader("type", message.getAttachmentMimeType());
            soapPart.addHeader("charset", "utf-8");
            soapPart.addHeader("Content-Transfer-Encoding", "binary");
            mimeMP.addBodyPart(soapPart);

            for (Attachment att : message.getAttachments()) {
                MimeBodyPart part = new MimeBodyPart();
                part.setDataHandler(att.getDataHandler());
                part.setContentID("<" + att.getId() + ">");
                if (att.isXOP()) {
                    part.addHeader("Content-Transfer-Encoding", "binary");
                }
                mimeMP.addBodyPart(part);
            }
            mimeMessage.setContent(mimeMP);
            mimeMessage.writeTo(out, FILTER);
        } catch (MessagingException me) {
            throw new WebServiceException(me);
        } catch (IOException ioe) {
            throw new WebServiceException(ioe);
        }
        return mimeMP.getContentType();
    }



}
