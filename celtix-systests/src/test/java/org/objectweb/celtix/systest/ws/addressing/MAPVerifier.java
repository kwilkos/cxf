package org.objectweb.celtix.systest.ws.addressing;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.Names;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;


/**
 * Verifies presence of MAPs in the context.
 */
public class MAPVerifier implements LogicalHandler<LogicalMessageContext> {
    VerificationCache verificationCache;
    List<String> expectedExposedAs = new ArrayList<String>();
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
        boolean isOutbound = ContextUtils.isOutbound(context);
        String mapProperty = 
            (String)mapProperties.get(isOutbound 
                                      ? MAPTest.OUTBOUND_KEY
                                      : MAPTest.INBOUND_KEY);
        AddressingPropertiesImpl maps = 
            (AddressingPropertiesImpl)context.get(mapProperty);
        if (ContextUtils.isRequestor(context)) {
            if (isOutbound) {
                String exposeAs = getExpectedExposeAs(false);
                if (exposeAs != null) {
                    maps.exposeAs(exposeAs);
                }
            } else {
                String exposeAs = getExpectedExposeAs(true);
                String expected = exposeAs != null
                                  ? exposeAs
                                  : Names.WSA_NAMESPACE_NAME;
                if (maps.getNamespaceURI() != expected) {
                    verificationCache.put("Incoming version mismatch"
                                          + " expected: " + expected
                                          + " got: " + maps.getNamespaceURI());
                }
                exposeAs = null;
            }
        }
        verificationCache.put(MAPTest.verifyMAPs(maps, this));
    }
    
    private String getExpectedExposeAs(boolean remove) {
        int size = expectedExposedAs.size();
        return  size == 0 
                ? null
                : remove
                  ? expectedExposedAs.remove(size - 1)
                  : expectedExposedAs.get(size - 1);
    }
}
