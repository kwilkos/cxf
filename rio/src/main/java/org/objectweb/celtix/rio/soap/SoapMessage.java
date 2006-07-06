package org.objectweb.celtix.rio.soap;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.rio.Message;
import org.objectweb.celtix.rio.message.AbstractWrappedMessage;

public class SoapMessage extends AbstractWrappedMessage {

    public static final String CHARSET = "utf-8";
    
    private Map<QName, Element> headers = new HashMap<QName, Element>(); 
    
    private SoapVersion version;
    
    public SoapMessage(Message message) {
        super(message);
    }

    public SoapVersion getVersion() {
        return version;
    }

    public void setVersion(SoapVersion v) {
        this.version = v;
    }

    public Map<QName, Element> getHeaders() {
        return headers;
    }

    public Element getHeader(QName name) {
        return headers.get(name);
    }

    public void setHeader(QName name, Element headerElement) {
        headers.put(name, headerElement);
    }
}
