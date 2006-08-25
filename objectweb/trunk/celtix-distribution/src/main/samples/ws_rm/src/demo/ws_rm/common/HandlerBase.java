package demo.ws_rm.common;


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
 * Base handler logic for WS-RM demo.   
 */
public abstract class HandlerBase implements SOAPHandler<SOAPMessageContext> {

    protected static final String WSA_NAMESPACE_URI = 
        "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    protected static final String WSA_ACTION = "Action";
    protected static final String WSRM_NAMESPACE_URI = 
        "http://schemas.xmlsoap.org/ws/2005/02/rm";

    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }


    public void close(MessageContext context) {
    }

    public void destroy() {
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
            System.out.println("failed to determine WS-A Action: " + e);
        }
        return action;
    }
}

