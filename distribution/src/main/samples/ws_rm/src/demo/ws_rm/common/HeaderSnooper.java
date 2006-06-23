package demo.ws_rm.common;


import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.NamedNodeMap;


/**
 * Snoops SOAP headers.
 */
public class HeaderSnooper extends HandlerBase {

    public boolean handleMessage(SOAPMessageContext context) {
        snoop(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        snoop(context);
        return true;
    }

    /**
     * Snoops WS-* headers in the current message.
     * Synchronized to avoid the output for asynchrously retransmitted
     * messages being interleaved.
     */
    private synchronized void snoop(SOAPMessageContext context) {
        try {
            SOAPHeader header = 
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            if (header != null) {
                System.out.println(getMessageSummary(context));
                displayWSHeaders(header, WSA_NAMESPACE_URI, "WS-Addressing");
                displayWSHeaders(header, WSRM_NAMESPACE_URI, "WS-RM");
            }
        } catch (SOAPException se) {
            System.out.println("SOAP header snoop failed: " + se);
        }
    }

    /**
     * @return a text summary of message type (out-of-band, application level 
     * etc.)
     */
    protected String getMessageSummary(SOAPMessageContext context) {
        String action = getAction(context);
        return getDirection(context) 
               + " Headers "
               + (action != null
                  ? action.startsWith(WSRM_NAMESPACE_URI)
                    ? "[out-of-band RM protocol message]"
                    : "[application-level message]"
                  : "[partial response]"); 
    }

    /**
     * @return a String specifying the direction of the current message
     */
    private String getDirection(SOAPMessageContext context) {
        return isOutbound(context)
               ? "\nOutbound"
               : "\nInbound";
    }

    /**
     * Display WS headers.
     */
    private void displayWSHeaders(SOAPHeader header,
                                  String uri,
                                  String display) throws SOAPException {
        System.out.println("  " + display);
        Iterator headerElements = header.examineAllHeaderElements();
        boolean found = false;
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement =
                (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            if (uri.equals(headerName.getURI())) {
                found = true;
                System.out.println("    " + headerName.getLocalName()
                                   + getText(headerElement));
            }
        }
        if (!found) {
            System.out.println("    None");
        }
    }

    /**
     * @return a text summary of header element content 
     */
    private String getText(SOAPHeaderElement headerElement) {
        String text = " : ";
        Iterator children = headerElement.getChildElements();
        while (children.hasNext()) {
            Node n = (Node)children.next();
            text += n.getLocalName() != null ? n.getLocalName() + "=" : "";
            text += getValue(n) + " "; 
        }
        return text;
    }

    /**
     * @return either the element value or a list of attribute values
     */
    private String getValue(Node node) {
        String value = "";
        if (node.getValue() != null) {
            value = node.getValue();
        } else {
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    org.w3c.dom.Node attr = attributes.item(i); 
                    value += attr.getTextContent();
                    if (i + 1 != attributes.getLength()) {
                        value += ",";
                    }
                }
            }
        }
        return value;
    }
}

