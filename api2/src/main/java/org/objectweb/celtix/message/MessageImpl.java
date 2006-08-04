package org.objectweb.celtix.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;

public class MessageImpl extends HashMap<String, Object> implements Message {
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private Conduit conduit;
    private Destination destination;
    private Exchange exchange;
    private String id;
    private InterceptorChain interceptorChain;
    private Map<Class<?>, Object> contents = new HashMap<Class<?>, Object>();
    
    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public String getAttachmentMimeType() {
        //for sub class overriding
        return null;
    }
    
    public Conduit getConduit() {
        return conduit;
    }

    public Destination getDestination() {
        return destination;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public String getId() {
        return id;
    }

    public InterceptorChain getInterceptorChain() {
        return this.interceptorChain;
    }

    public <T> T getContent(Class<T> format) {
        return format.cast(contents.get(format));
    }

    public <T> void setContent(Class<T> format, Object content) {
        contents.put(format, content);
    }

    public Set<Class<?>> getContentFormats() {
        return contents.keySet();
    }

    public void setConduit(Conduit c) {
        this.conduit = c;
    }

    public void setDestination(Destination d) {
        this.destination = d;
    }

    public void setExchange(Exchange e) {
        this.exchange = e;
    }

    public void setId(String i) {
        this.id = i;
    }

    public void setInterceptorChain(InterceptorChain ic) {
        this.interceptorChain = ic;
    }
    
}
