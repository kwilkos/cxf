package org.objectweb.celtix.bus.bindings.xml;

import javax.xml.ws.handler.MessageContext;

interface XMLMessageContext extends MessageContext {
    XMLMessage getMessage();
    void setMessage(XMLMessage message);
}
                                           
