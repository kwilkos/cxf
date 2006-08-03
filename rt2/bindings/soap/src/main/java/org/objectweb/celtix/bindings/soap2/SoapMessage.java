package org.objectweb.celtix.bindings.soap2;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.message.AbstractWrappedMessage;
import org.objectweb.celtix.message.Message;

public class SoapMessage extends AbstractWrappedMessage {
    
    private Map<Class, Object> headers = new HashMap<Class, Object>(); 
    
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

    public <T> T getHeaders(Class<T> format) {
        return format.cast(headers.get(format));
    }  

    public <T> void setHeaders(Class<T> format, T content) {
        headers.put(format, content);
    }
    
    public String getAttachmentMimeType() {
        return version.getSoapMimeType();
    }
    
}
