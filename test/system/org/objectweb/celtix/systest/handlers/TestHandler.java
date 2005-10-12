package org.objectweb.celtix.systest.handlers;

import java.util.Map;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;


class TestHandler<T extends LogicalMessageContext> implements LogicalHandler<T> {

    private int handleMessageInvoked; 
    private int handleFaultInvoked; 
    private int closeInvoked; 
    private int initInvoked; 
    private int destroyInvoked; 
    private boolean handleMessageRet = true; 
    
    
    public boolean handleMessage(T arg0) {
        handleMessageInvoked++;
        return handleMessageRet;
    }

    public boolean handleFault(T arg0) {
        handleFaultInvoked++;
        return true;
    }

    public void close(MessageContext arg0) {
        closeInvoked++;
    }

    public void init(Map arg0) {
        initInvoked++;
    }

    public void destroy() {
        destroyInvoked++;
    }

    public boolean isCloseInvoked() {
        return closeInvoked != 0;
    }

    public boolean isDestroyInvoked() {
        return destroyInvoked != 0;
    }

    public boolean isHandleFaultInvoked() {
        return handleFaultInvoked != 0;
    }

    public boolean isHandleMessageInvoked() {
        return handleMessageInvoked != 0;
    }

    public int getHandleMessageInvoked() {
        return handleMessageInvoked;
    }
    
    public boolean isInitInvoked() {
        return initInvoked != 0;
    }
    
    public void setHandleMessageRet(boolean ret) { 
        handleMessageRet = ret; 
    }
}    
