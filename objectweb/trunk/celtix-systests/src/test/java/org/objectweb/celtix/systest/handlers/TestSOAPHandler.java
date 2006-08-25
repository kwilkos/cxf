package org.objectweb.celtix.systest.handlers;


import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.objectweb.handler_test.PingException;

/**
 * Describe class TestSOAPHandler here.
 *
 *
 * Created: Fri Oct 21 13:24:05 2005
 *
 * @author <a href="mailto:codea@iona.com">codea</a>
 * @version 1.0
 */
public class  TestSOAPHandler<T extends SOAPMessageContext> extends TestHandlerBase 
    implements SOAPHandler<T> {

    public TestSOAPHandler() {
        this(true); 
    } 

    public TestSOAPHandler(boolean serverSide) {
        super(serverSide);
    }

    // Implementation of javax.xml.ws.handler.soap.SOAPHandler

    public final Set<QName> getHeaders() {
        return null;
    }
  
    public String getHandlerId() { 
        return "soapHandler" + getId();
    }
    
    public boolean handleMessage(T ctx) {

        boolean continueProcessing = true; 

        try {
            methodCalled("handleMessage"); 
            printHandlerInfo("handleMessage", isOutbound(ctx));
            Object b  = ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            boolean outbound = (Boolean)b;
            SOAPMessage msg = ctx.getMessage();

            if (isServerSideHandler()) {
                java.net.URI wsdlDescription = (java.net.URI) ctx.get(MessageContext.WSDL_DESCRIPTION);
                if (wsdlDescription == null) {
                    throw new PingException("WSDLDescription not found");
                }
                if (outbound) {
                    continueProcessing = true;
                } else {
                    continueProcessing = getReturnValue(outbound, ctx); 
                    if (!continueProcessing) {
                        outbound = true;
                    }
                }

                if (outbound) {
                    try {
                        // append handler id to SOAP response message 
                        SOAPBody body = msg.getSOAPBody(); 
                        Node resp = body.getFirstChild();

                        if (resp.getNodeName().contains("pingResponse")) { 
                            Node child = resp.getFirstChild();
                            Document doc = resp.getOwnerDocument();
                            Node info = doc.createElementNS(child.getNamespaceURI(), child.getLocalName());
                            info.setPrefix("ns4");
                            info.appendChild(doc.createTextNode(getHandlerId()));
                            resp.appendChild(info); 
                            msg.saveChanges();
                        } 
                    } catch (DOMException e) {
                        e.printStackTrace();
                    }
                } else {
                    getHandlerInfoList(ctx).add(getHandlerId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return continueProcessing;
    }

    public final boolean handleFault(T ctx) {
        methodCalled("handleFault"); 
        printHandlerInfo("handleFault", isOutbound(ctx));
        return true;
    }

    public final void init(final Map map) {
        methodCalled("init"); 

    }

    public final void destroy() {
        methodCalled("destroy"); 
    }

    public final void close(MessageContext messageContext) {
        methodCalled("close"); 
    }

    private boolean getReturnValue(boolean outbound, T ctx) { 

        if (outbound) {
            return true; 
        } 

        boolean ret = true;
        try {
            SOAPMessage msg  = ctx.getMessage(); 
            SOAPBody body = msg.getSOAPBody();

            if (body.getFirstChild().getFirstChild() == null) {
                return true;
            }

            Node commandNode = body.getFirstChild().getFirstChild().getFirstChild();
            String arg = commandNode.getNodeValue(); 
            String namespace = body.getFirstChild().getFirstChild().getNamespaceURI(); 
            
            StringTokenizer strtok = new StringTokenizer(arg, " ");
            String hid = strtok.nextToken();
            String direction = strtok.nextToken();
            String command = strtok.nextToken();
            
            if (getHandlerId().equals(hid)
                && "inbound".equals(direction)) {
                if ("stop".equals(command)) {

                    // remove the incoming request body.
                    Document doc = body.getOwnerDocument(); 
                    // build the SOAP response for this message 
                    //
                    Node wrapper = doc.createElementNS(namespace, "pingResponse");
                    wrapper.setPrefix("ns4");
                    body.removeChild(body.getFirstChild());
                    body.appendChild(wrapper); 

                    for (String info : getHandlerInfoList(ctx)) {
                        // copy the the previously invoked handler list into the response.  
                        // Ignore this handlers information as it will be added again later.
                        //
                        if (!info.contains(getHandlerId())) {
                            Node newEl = doc.createElementNS(namespace, "HandlersInfo");
                            newEl.setPrefix("ns4");
                            newEl.appendChild(doc.createTextNode(info));
                            wrapper.appendChild(newEl); 
                        }
                    }
                    ret = false;
                } else if ("throw".equals(command)) {
                    //throwException(strtok.nextToken());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
            
        return ret;
    } 


    public String toString() { 
        return getHandlerId();
    } 
}
