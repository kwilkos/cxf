package demo.ws_rm.common;


import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.soap.SOAPMessageContext;


/**
 * Discards a protion of inbound application-level messages to simulate 
 * message loss. Note that out-of-band WS-RM protocol messages are always
 * left intact.  
 */
public class MessageLossSimulator extends HandlerBase {

    /**
     * Discard every second message
     */
    private static final int LOSS_FACTOR = 2;
    private int inboundMessageCount;

    public boolean handleMessage(SOAPMessageContext context) {
        return continueProcessing(context);
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    /**
     * @return true if the current message should not be discarded
     */
    private synchronized boolean continueProcessing(SOAPMessageContext context) {
        if (!(isOutbound(context) || isRMOutOfBand(context))
            && ++inboundMessageCount % LOSS_FACTOR == 0) {
            discardWSHeaders(context);
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
            System.out.println("discard WS headers failed: " + e);
        }
    }
}
