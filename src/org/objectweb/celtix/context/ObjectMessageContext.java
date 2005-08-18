package org.objectweb.celtix.context;

import javax.xml.ws.handler.MessageContext;

public interface ObjectMessageContext extends MessageContext {
    
    Object[] getMessageObjects();
    
    void setMessageObjects(Object ... objects);

}
