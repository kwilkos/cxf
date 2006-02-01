package org.objectweb.celtix.systest.ws.addressing;


import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;


/**
 * Verifies presence of MAPs in the context.
 */
public class MAPVerifier implements LogicalHandler<LogicalMessageContext> {
    VerificationCache verificationCache;
    private Map<String, Object> mapProperties;        

    public MAPVerifier() {
        mapProperties = new HashMap<String, Object>();
        mapProperties.put(MAPTest.INBOUND_KEY, CLIENT_ADDRESSING_PROPERTIES_INBOUND);
        mapProperties.put(MAPTest.OUTBOUND_KEY, CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
    }
    
    public void init(Map<String, Object> params) {
        if (params != null && params.size() > 0) { 
            mapProperties = params;
        }
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
        AddressingProperties maps = 
            (AddressingProperties)context.get(mapProperty);
        verificationCache.put(MAPTest.verifyMAPs(maps, this));
    }
}
