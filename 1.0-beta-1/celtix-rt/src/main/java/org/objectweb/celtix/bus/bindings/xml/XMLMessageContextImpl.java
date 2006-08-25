package org.objectweb.celtix.bus.bindings.xml;

import java.util.*;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.MessageContextWrapper;

class XMLMessageContextImpl extends MessageContextWrapper implements XMLMessageContext {
    private static final long serialVersionUID = 1L;
    private static final String XML_MESSAGE = "org.objectweb.celtix.bindings.xml.message";

    public XMLMessageContextImpl(MessageContext ctx) {
        super(ctx);
    }
    
    public XMLMessage getMessage() {
        return (XMLMessage)get(XML_MESSAGE);
    }
    
    public void setMessage(XMLMessage xmlMsg) {
        put(XML_MESSAGE, xmlMsg);
        setScope(XML_MESSAGE, MessageContext.Scope.HANDLER);
    }
}
