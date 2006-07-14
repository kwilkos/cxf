package org.objectweb.celtix.bindings.soap2;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;

public class MustUnderstandInterceptor extends AbstractPhaseInterceptor {
        
    private SoapMessage soapMessage;    
    private Set<Element> mustUnderstandHeaders = new HashSet<Element>();
    private Set<QName> notUnderstandQNames = new HashSet<QName>();
    private Set<QName> mustUnderstandQNames;
    private Set<String> serviceRoles;
    
    public void handleMessage(Message message) {

        // TODO Auto-generated method stub
        soapMessage = (SoapMessage)message;
        if (!(soapMessage.getVersion() instanceof Soap12)) {            
            message.getInterceptorChain().doIntercept(message);
            return;
        }
        buildMustUnderstandHeaders();
        initServiceSideInfo();
        if (!checkUnderstand()) {
            StringBuffer sb = new StringBuffer(200);
            sb.append("Can't understands QNames: ");
            for (QName qname : notUnderstandQNames) {
                sb.append(qname.toString() + ", ");
            }
            SOAPException mustUnderstandException = new SOAPException(sb.toString());
            soapMessage.put(Message.INBOUND_EXCEPTION, mustUnderstandException);
        }
    }

    private void initServiceSideInfo() {
        if (mustUnderstandQNames != null) {
            return;
        } else {
            mustUnderstandQNames = new HashSet<QName>();
        }
        mustUnderstandQNames.addAll(ServiceModelUtil.getHeaderQNameInOperationParam(soapMessage));
        // Loop all SoapInterceptor, add roles into serviceRoles
        // And add understood QName to mustUnderstandQNames
        
    }

    private void buildMustUnderstandHeaders() {
        Element headers = (Element)soapMessage.getHeaders(Element.class);        
        for (int i = 0; i < headers.getChildNodes().getLength(); i++) {
            Element header = (Element)headers.getChildNodes().item(i);
            String mustUnderstand = header.getAttribute(Soap12.ATTRNAME_MUSTUNDERSTAND);
            if (Boolean.valueOf(mustUnderstand) || "1".equals(mustUnderstand.trim())) {
                String role = header.getAttribute(Soap12.ATTRNAME_ROLE);
                if (role != null) {
                    role = role.trim();
                    if (role.equals(Soap12.ROLE_NEXT) || role.equals(Soap12.ROLE_ULTIMATERECEIVER)) {
                        mustUnderstandHeaders.add(header);
                    } else {
                        for (String roleFromBinding : serviceRoles) {
                            if (role.equals(roleFromBinding)) {
                                mustUnderstandHeaders.add(header);
                            }
                        }
                    }
                } else {
                    // if role omitted, the soap node is ultimate receiver, needs to understand
                    mustUnderstandHeaders.add(header);
                }
            }
        }
    }

    private boolean checkUnderstand() {
        for (Element header : mustUnderstandHeaders) {
            QName qname = new QName(header.getNamespaceURI(), header.getLocalName());
            if (!mustUnderstandQNames.contains(qname)) {
                notUnderstandQNames.add(qname);
            }
        }
        if (notUnderstandQNames.size() > 0) {
            return false;
        }
        return true;
    }
}
