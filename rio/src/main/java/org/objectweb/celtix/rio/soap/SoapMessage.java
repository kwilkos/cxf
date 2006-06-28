package org.objectweb.celtix.rio.soap;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.objectweb.celtix.rio.Message;
import org.objectweb.celtix.rio.message.AbstractWrappedMessage;

public class SoapMessage extends AbstractWrappedMessage {

    public static final String CHARSET = "utf-8";
    
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

    public Collection getHeaders(QName name) {
        return null;
    }
    
    public void setHeaders(QName name, Collection headers) {
        
    }
}
