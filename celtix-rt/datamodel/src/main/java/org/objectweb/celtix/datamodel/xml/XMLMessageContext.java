package org.objectweb.celtix.datamodel.xml;

import javax.xml.ws.handler.MessageContext;

public interface XMLMessageContext extends MessageContext {
    XMLMessage getMessage();
    void setMessage(XMLMessage message);
}
                                           
