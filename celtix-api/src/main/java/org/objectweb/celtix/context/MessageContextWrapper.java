package org.objectweb.celtix.context;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.handler.MessageContext;

public class MessageContextWrapper implements MessageContext {
    protected MessageContext context;
    
    public MessageContextWrapper(MessageContext ctx) {
        context = ctx;
    }

    public String toString() {
        String wrapped = context instanceof MessageContextWrapper 
                         ? context.toString()
                         : context.getClass().getName();
        return this.getClass().getName() + " wrapping: <" + wrapped + ">";
    }

    public void setScope(String arg0, Scope arg1) {
        context.setScope(arg0, arg1);
    }

    public Scope getScope(String arg0) {
        return context.getScope(arg0);
    }

    public int size() {
        return context.size();
    }

    public boolean isEmpty() {
        return context.isEmpty();
    }

    public boolean containsKey(Object arg0) {
        return context.containsKey(arg0);
    }

    public boolean containsValue(Object arg0) {
        return context.containsValue(arg0);
    }

    public Object get(Object arg0) {
        return context.get(arg0);
    }

    public Object put(String arg0, Object arg1) {
        return context.put(arg0, arg1);
    }

    public Object remove(Object arg0) {
        return context.remove(arg0);
    }

    public void putAll(Map<? extends String, ? extends Object> arg0) {
        context.putAll(arg0);
    }

    public void clear() {
        context.clear();
    }

    public Set<String> keySet() {
        return context.keySet();
    }

    public Collection<Object> values() {
        return context.values();
    }

    public Set<Entry<String, Object>> entrySet() {
        return context.entrySet();
    }


    public MessageContext getWrappedContext() { 
        return context;
    }
    
    public static MessageContext unwrap(MessageContext ctx) { 
        MessageContext wrapper = ctx;
        while (wrapper instanceof MessageContextWrapper) {
            wrapper = ((MessageContextWrapper)wrapper).getWrappedContext();
        }
        return wrapper;
    }
}
