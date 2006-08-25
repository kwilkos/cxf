package org.objectweb.celtix.systest.ws.rm;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;




/**
 * Discards a protion of inbound application-level messages to simulate 
 * message loss. Note that out-of-band WS-RM protocol messages are always
 * left intact.  
 */
public class MessageLossSimulator implements SOAPHandler<SOAPMessageContext> {
    protected static final String WSA_NAMESPACE_URI = 
        "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    protected static final String WSA_ACTION = "Action";
    protected static final String WSRM_NAMESPACE_URI = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm";
    
    /**
     * Discard every second message
     */
    private static final int LOSS_FACTOR = 2;
    private int inboundMessageCount;

    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }


    public void close(MessageContext context) {
    }

    public void destroy() {
    }
    
    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println("*** MessageLoss: handling message");
        return continueProcessing(context);
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }
    
    /**
     * @return true if the current message is outbound
     */
    protected boolean isOutbound(SOAPMessageContext context) {
        Boolean outbound = (Boolean)context.get(MESSAGE_OUTBOUND_PROPERTY);
        return outbound != null && outbound.booleanValue();
    }

    /**
     * @return the WS-A Action header
     */
    protected String getAction(SOAPMessageContext context) {
        String action = null;
        try {
            SOAPHeader header =  
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            Iterator headerElements = header.examineAllHeaderElements();
            while (headerElements.hasNext()) {
                SOAPHeaderElement headerElement =
                    (SOAPHeaderElement)headerElements.next();
                Name headerName = headerElement.getElementName();
                if (WSA_NAMESPACE_URI.equals(headerName.getURI())
                    && WSA_ACTION.equals(headerName.getLocalName())) {
                    Iterator children = headerElement.getChildElements();
                    if (children.hasNext()) {
                        action = ((Node)children.next()).getValue();
                    }
                }
            }
        } catch (SOAPException e) {
            System.out.println("*** failed to determine WS-A Action: " + e);
        }
        return action;
    }


    /**
     * @return true if the current message should not be discarded
     */
    private synchronized boolean continueProcessing(SOAPMessageContext context) {
        System.out.println("*** inboundMessageCount: " + inboundMessageCount);         
        if (!(isOutbound(context) || isRMOutOfBand(context))
            && ++inboundMessageCount % LOSS_FACTOR == 0 && inboundMessageCount <= 4) {
            discardWSHeaders(context);
            discardBody(context);
            System.out.println("*** Discarding current inbound message ***");
            return false;
        }
        return true;
    }

    /**
     * @return true if this is a WS-RM out-of-band protocol message
     */
    protected boolean isRMOutOfBand(SOAPMessageContext context) {
        String action = getAction(context);
        return action != null && action.startsWith(WSRM_NAMESPACE_URI);
    }

    /**
     * Discard any WS-* headers from the message
     */
    private void discardWSHeaders(SOAPMessageContext context) {
        try {
            SOAPHeader header =  
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            Iterator headerElements = header.examineAllHeaderElements();
            while (headerElements.hasNext()) {
                SOAPHeaderElement headerElement =
                    (SOAPHeaderElement)headerElements.next();
                Name headerName = headerElement.getElementName();
                if (WSRM_NAMESPACE_URI.equals(headerName.getURI())
                    || WSRM_NAMESPACE_URI.equals(headerName.getURI())) {
                    headerElement.detachNode();
                }
            }
        } catch (SOAPException e) {
            System.out.println("*** discard WS headers failed: " + e);
        }
    }
    
    
    /**
     * Discard the body from the message to avoid assertion failure when
     * unmarshaling partial response (occuring when system tests are run in
     * fork mode 'none')
     */
    private void discardBody(SOAPMessageContext context) {
        try {
            context.getMessage().getSOAPBody().removeContents();
        } catch (SOAPException e) {
            System.out.println("*** discard body failed: " + e);
        }
    }
}
