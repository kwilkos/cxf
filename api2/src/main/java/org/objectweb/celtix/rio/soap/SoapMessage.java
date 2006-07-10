package org.objectweb.celtix.rio.soap;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.rio.Message;
import org.objectweb.celtix.rio.message.AbstractWrappedMessage;

public class SoapMessage extends AbstractWrappedMessage {

    public static final String CHARSET = "utf-8";
    
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
  
}
