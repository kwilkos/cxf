package org.objectweb.celtix.bindings.soap2.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.rio.Attachment;
import org.objectweb.celtix.rio.soap.Soap11;
import org.objectweb.celtix.rio.soap.Soap12;
import org.objectweb.celtix.rio.soap.SoapMessage;
import org.objectweb.celtix.rio.soap.SoapVersion;

public final class AttachmentUtil {

    private static final String[] FILTER = new String[] {"Message-ID", "Mime-Version", "Content-Type"};    

    private AttachmentUtil() {
    }

    /**
     * Using result & attachment in soapMessage to write to output stream
     * 
     * @param soapMessage
     * @param out
     * @throws CxfRioException
     */

    public static String serializeMultipartMessage(SoapMessage soapMessage, OutputStream out)
        throws MessagingException, IOException {
        
        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);
        String soapPartId = createContentID(null);
        String subType = getMimeSubType(soapMessage.getVersion(), soapPartId);
        MimeMultipart mimeMP = new MimeMultipart(subType);
               
        InputStream in = (InputStream) soapMessage.getResult(InputStream.class);
        AttachmentDataSource ads = new AttachmentDataSource("application/xop+xml", in);
        MimeBodyPart soapPart = new MimeBodyPart();
        soapPart.setDataHandler(new DataHandler(ads));
        soapPart.setContentID("<" + soapPartId  + ">");
        soapPart.addHeader("Content-Type", "application/xop+xml");        
        soapPart.addHeader("type", soapMessage.getVersion().getSoapMimeType());
        soapPart.addHeader("charset", SoapMessage.CHARSET);
        soapPart.addHeader("Content-Transfer-Encoding", "binary");
        mimeMP.addBodyPart(soapPart);
        
        for (Iterator itr = soapMessage.getAttachments().iterator(); itr.hasNext();) {
            Attachment att = (Attachment)itr.next();
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
        return mimeMP.getContentType();
    }

    /**
     * determine whether the input message is a xop enabled message
     * 
     * @param message
     * @return
     */
    public static boolean isXOPPackage(SoapMessage message) {
        String contentTypeOfSoapBodyPart;
        String typeOfSoapBodyPart;
        Attachment primaryMimePart = message.getSource(Attachment.class);
        if (primaryMimePart == null) {
            return false;
        } else {
            contentTypeOfSoapBodyPart = primaryMimePart.getHeader("Content-Type");
        }
        if ("application/xop+xml".equals(contentTypeOfSoapBodyPart)) {
            typeOfSoapBodyPart = primaryMimePart.getHeader("type");
            if (message.getVersion() instanceof Soap12
                && typeOfSoapBodyPart.indexOf("application/soap+xml") >= 0) {
                return true;
            } else if (message.getVersion() instanceof Soap11 
                && typeOfSoapBodyPart.indexOf("text/xml") >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * create MimeMultipart to represent the soap message
     * 
     * @param message
     * @return
     */
    public static String getMimeSubType(SoapVersion soapVersion, String soapPartId) {
        StringBuffer ct = new StringBuffer();
        ct.append("related; ");
        ct.append("type=\"application/xop+xml\"; ");
        ct.append("start=\"<" + soapPartId + ">\"; ");
        ct.append("start-info=\"" + soapVersion.getSoapMimeType() + "\"");
        return ct.toString();
    }

    /**
     * @param ns
     * @return
     */
    public static String createContentID(String ns) {
        // tend to change
        String cid = "celtix.objectweb.org";
        String name = UUID.randomUUID() + "@";
        if (ns != null && (ns.length() > 0)) {
            try {
                URI uri = new URI(ns);
                String host = uri.toURL().getHost();
                cid = host;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                try {
                    cid = URLEncoder.encode(ns, SoapMessage.CHARSET);
                } catch (UnsupportedEncodingException e1) {
                    throw new WebServiceException("Encoding content id with namespace error", e);
                }
            }
        }
        return name + cid;
    }

}
