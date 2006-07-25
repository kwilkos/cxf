package org.objectweb.celtix.bindings.attachments;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.xml.ws.WebServiceException;

public final class AttachmentUtil {

    private AttachmentUtil() {
        
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
                    cid = URLEncoder.encode(ns, "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    throw new WebServiceException("Encoding content id with namespace error", e);
                }
            }
        }
        return name + cid;
    }

}
