package org.objectweb.celtix.bus.bindings.soap;

import javax.xml.namespace.QName;

public final class SOAPMessageUtil {

    private SOAPMessageUtil() {
        // Utility class - never constructed
    }
    
    public static String createWrapDocLitSOAPMessage(QName wrapName, QName elName, String data) {
        StringBuffer str = new StringBuffer();
        
        str.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        str.append("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" ");
        str.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        str.append("<SOAP-ENV:Body>");

        str.append("<ns4:" + wrapName.getLocalPart() + " xmlns:ns4=\"" + wrapName.getNamespaceURI() + "\">");
        if (elName != null) {
            str.append("<ns4:" + elName.getLocalPart() + ">");
            str.append(data);
            str.append("</ns4:" + elName.getLocalPart() + ">");
        }
        str.append("</ns4:" + wrapName.getLocalPart() + ">");
        
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");
        
        return str.toString();
    }
}
