package org.objectweb.celtix.rio.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.rio.Attachment;
import org.objectweb.celtix.rio.Channel;
import org.objectweb.celtix.rio.Exchange;
import org.objectweb.celtix.rio.InterceptorChain;
import org.objectweb.celtix.rio.Message;

/**
 * A base class to build your own message implementations on.
 * 
 * @author Dan
 */
public abstract class AbstractWrappedMessage implements Message {

    public static final String MIME_HTTP_HEADERS = "HTTP_HEADERS";
    public static final String INBOUND_EXCEPTION = "INBOUND_EXCEPTION";
    
    private Message message;

    protected AbstractWrappedMessage(Message msg) {
        this.message = msg;
    }

    public void clear() {
        message.clear();
    }

    public boolean containsKey(Object key) {
        return message.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return message.containsValue(value);
    }

    public Set<Entry<String, Object>> entrySet() {
        return message.entrySet();
    }

    public boolean equals(Object o) {
        return message.equals(o);
    }

    public Object get(Object key) {
        return message.get(key);
    }

    public Collection<Attachment> getAttachments() {
        return message.getAttachments();
    }

    public Channel getChannel() {
        return message.getChannel();
    }

    public Exchange getExchange() {
        return message.getExchange();
    }

    public String getId() {
        return message.getId();
    }

    public InterceptorChain getInterceptorChain() {
        return message.getInterceptorChain();
    }

    public <T> T getResult(Class<T> format) {
        return message.getResult(format);
    }

    public Set<Class> getResultFormats() {
        return message.getResultFormats();
    }

    public <T> T getSource(Class<T> format) {
        return message.getSource(format);
    }

    public Set<Class> getSourceFormats() {
        return message.getSourceFormats();
    }

    public int hashCode() {
        return message.hashCode();
    }

    public boolean isEmpty() {
        return message.isEmpty();
    }

    public Set<String> keySet() {
        return message.keySet();
    }

    public Object put(String key, Object value) {
        return message.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        message.putAll(t);
    }

    public Object remove(Object key) {
        return message.remove(key);
    }

    public <T> void setResult(Class<T> format, Object content) {
        message.setResult(format, content);
    }

    public <T> void setSource(Class<T> format, Object content) {
        message.setSource(format, content);
    }

    public int size() {
        return message.size();
    }

    public Collection<Object> values() {
        return message.values();
    }
    
}
