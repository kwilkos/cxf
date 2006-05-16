package org.objectweb.celtix.bus.handlers;

import javax.annotation.Resource;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

public class TestHandler<T extends LogicalMessageContext> implements LogicalHandler<T> {

    public static final String STRING_PARAM_NAME = "stringParam"; 
    public static final String STRING_PARAM_VALUE = "stringValue"; 

    @Resource
    private String stringParam; 
    
    public String getStringParam() { 
        return stringParam;    
    }
    
    public void setStringParam(String str) {
        stringParam = str;
    }
    public void close(MessageContext arg0) {
    }

    public void destroy() {
    }

    public boolean handleFault(T arg0) {
        return false;
    }

    public boolean handleMessage(T arg0) {
        return false;
    }

}
