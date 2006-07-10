package org.objectweb.celtix.rio.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.rio.Attachment;
import org.objectweb.celtix.rio.Channel;
import org.objectweb.celtix.rio.Exchange;
import org.objectweb.celtix.rio.InterceptorChain;
import org.objectweb.celtix.rio.Message;

public class MessageImpl extends HashMap<String, Object> implements Message {
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private Channel channel;
    private Exchange exchange;
    private String id;
    private InterceptorChain interceptorChain;
    private Map<Class, Object> results = new HashMap<Class, Object>();
    private Map<Class, Object> sources = new HashMap<Class, Object>();
    
    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public Channel getChannel() {
        return channel;
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

    @SuppressWarnings("unchecked")
    public <T> T getResult(Class<T> format) {
        return (T) results.get(format);
    }

    public <T> void setResult(Class<T> format, Object content) {
        results.put(format, content);
    }

    public Set<Class> getResultFormats() {
        return results.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T> T getSource(Class<T> format) {
        return (T) sources.get(format);
    }

    public Set<Class> getSourceFormats() {
        return sources.keySet();
    }

    public void setChannel(Channel c) {
        this.channel = c;
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

    public <T> void setSource(Class<T> format, Object content) {
        sources.put(format, content);
    }
    
}
