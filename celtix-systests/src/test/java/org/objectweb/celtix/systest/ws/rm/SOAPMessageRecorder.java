package org.objectweb.celtix.systest.ws.rm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;

public class SOAPMessageRecorder implements SOAPHandler<SOAPMessageContext> {
    
    private static List<SOAPMessage> outbound;
    private static List<SOAPMessage> inbound;
    
    public SOAPMessageRecorder() {
        if (null == outbound) {
            outbound = new ArrayList<SOAPMessage>();
        }
        if (null == inbound) {
            inbound = new ArrayList<SOAPMessage>();
        }
    }
    
    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        record(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        record(context);
        return true;
    }

    public void close(MessageContext arg0) {
    }
    
    protected List<SOAPMessage> getInboundMessages() {
        return inbound;
    }
    
    protected List<SOAPMessage> getOutboundMessages() {
        return outbound;
    }
    
    private void record(SOAPMessageContext context) {
        SOAPMessage sm = context.getMessage();
        if (ContextUtils.isOutbound(context)) {            
            outbound.add(sm);
        } else {
            inbound.add(sm);
        }
    }
    
    
    
    

}
