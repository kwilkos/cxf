package org.objectweb.celtix.jaxws.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.objectweb.celtix.message.Message;

public class WrappedMessageContext implements MessageContext {

    private Message message;
    private Map<String, Scope> scopes = new HashMap<String, Scope>();

    public WrappedMessageContext(Message m) {
        message = m;
    }
    
    public Message getWrappedMessage() {
        return message;
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

    public Object get(Object key) {
        return message.get(key);
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

    public int size() {
        return message.size();
    }

    public Collection<Object> values() {
        return message.values();
    }

    public void setScope(String arg0, Scope arg1) {
        if (!this.containsKey(arg0)) {
            throw new IllegalArgumentException("non-existant property-" + arg0 + "is specified");    
        }
        scopes.put(arg0, arg1);        
    }

    public Scope getScope(String arg0) {
        
        if (containsKey(arg0)) {
            if (scopes.containsKey(arg0)) {
                return scopes.get(arg0);
            } else {
                return Scope.HANDLER;
            }
        }
        throw new IllegalArgumentException("non-existant property-" + arg0 + "is specified");
    }
    
    
}
