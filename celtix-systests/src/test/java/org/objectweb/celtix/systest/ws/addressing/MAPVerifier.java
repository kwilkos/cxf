package org.objectweb.celtix.systest.ws.addressing;


import java.util.Map;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;


/**
 * Verifies presence of MAPs in the context.
 */
public class MAPVerifier implements LogicalHandler<LogicalMessageContext> {
    VerificationCache verificationCache;
    private Map<String, Object> mapProperties;        

    public void init(Map<String, Object> params) {
        mapProperties = params;
    }

    public boolean handleMessage(LogicalMessageContext context) {
        verify(context);
        return true;
    }

    public boolean handleFault(LogicalMessageContext context) {
        verify(context);
        return true;
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }

    private void verify(LogicalMessageContext context) {
        String mapProperty = 
            (String)mapProperties.get(ContextUtils.isOutbound(context) 
                                      ? MAPTest.OUTBOUND_KEY
                                      : MAPTest.INBOUND_KEY);
        //System.out.println("map property: " + mapProperty);
        AddressingProperties maps = 
            (AddressingProperties)context.get(mapProperty);
        verificationCache.put(MAPTest.verifyMAPs(maps, this));
    }
}
