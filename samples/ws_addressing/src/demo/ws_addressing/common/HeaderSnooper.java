package demo.ws_addressing.common;


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

import org.objectweb.celtix.bus.ws.addressing.Names;


/**
 * Snoops SOAP headers.
 */
public class HeaderSnooper implements SOAPHandler<SOAPMessageContext> {

    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        snoop(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        snoop(context);
        return true;
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }

    private void snoop(SOAPMessageContext context) {
        try {
            SOAPHeader header = 
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            if (header != null) {
                System.out.println(getDirection(context)
                                   + " WS-Addressing headers");
                Iterator headerElements = header.examineAllHeaderElements();
                while (headerElements.hasNext()) {
                    SOAPHeaderElement headerElement =
                        (SOAPHeaderElement)headerElements.next();
                    Name headerName = headerElement.getElementName();
                    if (Names.WSA_NAMESPACE_NAME.equals(headerName.getURI())) {
                        System.out.println(headerName.getLocalName()
                                           + getText(headerElement));
                    }
                }
                System.out.println();
            }
        } catch (SOAPException se) {
            System.out.println("SOAP header snoop failed: " + se);
        }
    }

    private String getDirection(SOAPMessageContext context) {
        Boolean outbound = (Boolean)context.get(MESSAGE_OUTBOUND_PROPERTY);
        return outbound != null && outbound.booleanValue()
               ? "Outbound"
               : "Inbound";
    }

    private String getText(SOAPHeaderElement headerElement) {
        String text = " : ";
        Iterator children = headerElement.getChildElements();
        if (children.hasNext()) {
            text += ((Node)children.next()).getValue();            
        }
        return text;
    }

}

