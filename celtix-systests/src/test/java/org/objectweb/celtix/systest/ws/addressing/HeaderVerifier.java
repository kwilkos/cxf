package org.objectweb.celtix.systest.ws.addressing;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bus.ws.addressing.Names;


/**
 * Verifies presence of expected SOAP headers.
 */
public class HeaderVerifier implements SOAPHandler<SOAPMessageContext> {
    VerificationCache verificationCache;

    public void init(Map<String, Object> map) {
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        verify(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        verify(context);
        return true;
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }

    private void verify(SOAPMessageContext context) {
        try {
            List<String> wsaHeaders = new ArrayList<String>();
            SOAPHeader header = 
                context.getMessage().getSOAPPart().getEnvelope().getHeader();
            if (header != null) {
                Iterator headerElements = header.examineAllHeaderElements();
                while (headerElements.hasNext()) {
                    Name headerName = ((SOAPHeaderElement)headerElements.next()).getElementName();
                    if (Names.WSA_NAMESPACE_NAME.equals(headerName.getURI())) {
                        wsaHeaders.add(headerName.getLocalName());
                    }
                }
            }
            verificationCache.put(MAPTest.verifyHeaders(wsaHeaders));
        } catch (SOAPException se) {
            verificationCache.put("SOAP header verification failed: " + se);
        }
    }
}
