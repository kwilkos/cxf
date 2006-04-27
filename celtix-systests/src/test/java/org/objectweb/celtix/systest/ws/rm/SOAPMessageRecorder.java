package org.objectweb.celtix.systest.ws.rm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.context.StreamMessageContext;
import org.objectweb.celtix.handlers.StreamHandler;

public class SOAPMessageRecorder implements StreamHandler {
    
    private static List<SOAPMessage> outbound;
    
    public SOAPMessageRecorder() {
        if (null == outbound) {
            outbound = new ArrayList<SOAPMessage>();
        }
    }
    
    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(StreamMessageContext context) {
        record(context);
        return true;
    }

    public boolean handleFault(StreamMessageContext context) {
        record(context);
        return true;
    }

    public void close(MessageContext arg0) {
    }
    
    protected List<SOAPMessage> getOutboundMessages() {
        return outbound;
    }
    
    private void record(StreamMessageContext context) {        
        if (ContextUtils.isOutbound(context)) { 
            SOAPMessage sm = (SOAPMessage)context.get("org.objectweb.celtix.bindings.soap.message");
            outbound.add(sm);
        }
    }
    
    
    
    

}
