package org.apache.cxf.bindings.attachments;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.xml.ws.WebServiceException;

import org.apache.cxf.message.Message;

public final class AttachmentUtil {

    private AttachmentUtil() {

    }

    /**
     * @param ns
     * @return
     */
    public static String createContentID(String ns) {
        // tend to change
        String cid = "cxf.apache.org";
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
                    cid = URLEncoder.encode(ns, "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    throw new WebServiceException("Encoding content id with namespace error", e);
                }
            }
        }
        return name + cid;
    }

    public static String getUniqueBoundaryValue(int part) {
        StringBuffer s = new StringBuffer();
        // Unique string is ----=_Part_<part>_<hashcode>.<currentTime>
        s.append("----=_Part_").append(part++).append("_").append(s.hashCode()).append('.')
            .append(System.currentTimeMillis());
        return s.toString();
    }

    public static String getMimePartHeader(Message message, String soapPartId, String action) {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append("Content-Type: application/xop+xml; charset=utf-8; ");
        buffer.append("type=\"" + message.getAttachmentMimeType());
        if (action != null) {
            buffer.append("; action=" + action + "\"\n");
        } else {
            buffer.append("\"\n");
        }
        buffer.append("Content-Transfer-Encoding: binary");
        buffer.append("Content-ID: <" + soapPartId + ">");
        return buffer.toString();
    }

    public static String getMimeRequestHeader(Message message, String soapPartId, String contentDesc) {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append("MIME-Version: 1.0\n");
        buffer.append("Content-Type: Multipart/" + getMimeSubType(message, soapPartId) + "\n");
        buffer.append("Content-Description: " + contentDesc + "\n");
        return buffer.toString();
    }

    public static String getMimeSubType(Message message, String soapPartId) {
        StringBuffer ct = new StringBuffer();
        ct.append("related; ");
        ct.append("type=\"application/xop+xml\"; ");
        ct.append("start=\"<" + soapPartId + ">\"; ");
        ct.append("start-info=\"" + message.getAttachmentMimeType() + "\"");
        return ct.toString();
    }
}
