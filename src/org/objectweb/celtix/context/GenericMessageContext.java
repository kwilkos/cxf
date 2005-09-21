package org.objectweb.celtix.context;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

public class GenericMessageContext extends HashMap<String, Object> implements MessageContext {
    private static final long serialVersionUID = 1L;
    
    protected Map<String, Scope> scopes = new HashMap<String, Scope>();

    public void setScope(String arg0, Scope arg1) {
        if (!this.containsKey(arg0)) {
            throw new IllegalArgumentException("non-existant property-" + arg0 + "is specified");    
        }
        scopes.put(arg0, arg1);        
    }

    public Scope getScope(String arg0) {
        if (scopes.containsKey(arg0)) {
            return scopes.get(arg0);
        }
        throw new IllegalArgumentException("non-existant property-" + arg0 + "is specified");
    }

}
