package org.objectweb.celtix.bus.handlers;

import java.util.Map;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

public class DummyHandler implements Handler {

    Map config; 
    private boolean initCalled; 

    public DummyHandler() {

    }

    public final boolean handleMessage(final MessageContext messageContext) {
        return false;
    }

    public final boolean handleFault(final MessageContext messageContext) {
        return false;
    }

    public final void init(final Map map) {
        config = map; 
        initCalled = true; 
    }

    public final void destroy() {

    }

    public final void close(final MessageContext messageContext) {

    }


    public boolean initCalled() { 
        return initCalled;
    } 

    public final Map getConfig() {
        return config; 
    } 
}
