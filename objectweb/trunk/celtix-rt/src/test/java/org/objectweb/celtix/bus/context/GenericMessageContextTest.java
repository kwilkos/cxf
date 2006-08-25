package org.objectweb.celtix.bus.context;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.GenericMessageContext;

public class GenericMessageContextTest extends AbstractMessageContextTestBase {

    @Override
    protected MessageContext getMessageContext() {
        // TODO Auto-generated method stub
        return new GenericMessageContext();
    }

}
