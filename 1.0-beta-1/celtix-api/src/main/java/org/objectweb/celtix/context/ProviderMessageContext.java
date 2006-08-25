package org.objectweb.celtix.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

public class ProviderMessageContext implements Map<String, Object> {

    private static final long serialVersionUID = 1L;
    private final MessageContext context;

    public ProviderMessageContext(MessageContext ctx) {
        context = ctx;
    }

    public void clear() {
        Iterator<String> it = context.keySet().iterator();
        while (it.hasNext()) {
            String k = it.next();
            if (context.getScope(k) == Scope.APPLICATION) {
                context.remove(k);
            }
        }
    }

    public boolean containsKey(Object key) {
        return context.containsKey(key) && context.getScope((String)key) == Scope.APPLICATION;
    }

    public boolean containsValue(Object value) {
        if (!context.containsValue(value)) {
            return false;
        }
        Iterator<String> it = context.keySet().iterator();
        while (it.hasNext()) {
            String k = it.next();
            if (context.get(k) == value && context.getScope(k) == Scope.APPLICATION) {
                return true;
            }
        }
        return false;
    }

    public Set<Entry<String, Object>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object get(Object key) {
        Object o = context.get(key);
        if (context.getScope((String)key) == Scope.HANDLER) {
            return null;
        }
        return o;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Set<String> keySet() {
        Set<String> allKeys = context.keySet();
        Set<String> keys = new HashSet<String>();
        Iterator<String> it = allKeys.iterator();
        while (it.hasNext()) {
            String k = it.next();
            if (context.getScope(k) == Scope.APPLICATION) {
                keys.add(k);
            }
        }
        return keys;
    }

    public Object put(String key, Object value) {
        if (context.containsKey(key) && context.getScope(key) == Scope.HANDLER) {
            throw new IllegalArgumentException(
                "Attempt to set property with scope HANDLER in provider context.");
        }
        Object o = context.put(key, value);
        context.setScope(key, Scope.APPLICATION);
        return o;
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        Iterator<? extends String> it = t.keySet().iterator();
        while (it.hasNext()) {
            String k = it.next();
            put(k, t.get(k));
        }
    }

    public Object remove(Object key) {
        if (context.containsKey(key) && context.getScope((String)key) == Scope.HANDLER) {
            throw new IllegalArgumentException(
                "Attempt to remove property with scope HANDLER from provider context.");
        }
        return context.remove(key);
    }

    public int size() {
        return values().size();
    }

    public Collection<Object> values() {
        Collection<Object> values = new ArrayList<Object>();
        Iterator<? extends String> it = context.keySet().iterator();
        while (it.hasNext()) {
            String k = it.next();
            if (context.getScope(k) == Scope.APPLICATION) {
                values.add(context.get(k));
            }
        }
        return values;
    }
}
