package org.objectweb.celtix.bindings.soap2;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;

import org.objectweb.celtix.interceptors.Interceptor;

public class MustUnderstandInterceptor extends AbstractSoapInterceptor {

    private SoapMessage soapMessage;
    private Set<Element> mustUnderstandHeaders = new HashSet<Element>();
    private Set<QName> notUnderstandQNames = new HashSet<QName>();
    private Set<QName> mustUnderstandQNames;
    private Set<URI> serviceRoles = new HashSet<URI>();

    public void handleMessage(SoapMessage message) {

        // TODO Auto-generated method stub
        soapMessage = message;
        buildMustUnderstandHeaders();
        initServiceSideInfo();
        if (!checkUnderstand()) {
            StringBuffer sb = new StringBuffer(300);
            sb.append("Can't understands QNames: ");
            for (QName qname : notUnderstandQNames) {
                sb.append(qname.toString() + ", ");
            }
            SOAPException mustUnderstandException = new SOAPException(sb.toString());
            soapMessage.setContent(Exception.class, mustUnderstandException);
        }
    }

    private void initServiceSideInfo() {
        if (mustUnderstandQNames != null) {
            return;
        } else {
            mustUnderstandQNames = new HashSet<QName>();
        }
        Set<QName> paramHeaders = ServiceModelUtil.getHeaderQNameInOperationParam(soapMessage);
        if (paramHeaders != null) {
            mustUnderstandQNames.addAll(paramHeaders);
        }
        Iterator it = soapMessage.getInterceptorChain().getIterator();
        while (it.hasNext()) {
            Interceptor interceptorInstance = (Interceptor)it.next();
            if (interceptorInstance instanceof SoapInterceptor) {
                SoapInterceptor si = (SoapInterceptor)interceptorInstance;
                serviceRoles.addAll(si.getRoles());
                mustUnderstandQNames.addAll(si.getUnderstoodHeaders());
            }
        }
    }

    private void buildMustUnderstandHeaders() {
        Element headers = (Element)soapMessage.getHeaders(Element.class);
        for (int i = 0; i < headers.getChildNodes().getLength(); i++) {
            Element header = (Element)headers.getChildNodes().item(i);
            String mustUnderstand = header.getAttributeNS(soapMessage.getVersion().getNamespace(),
                                                          soapMessage.getVersion()
                                                              .getAttrNameMustUnderstand());

            if (Boolean.valueOf(mustUnderstand) || "1".equals(mustUnderstand.trim())) {
                String role = header.getAttributeNS(soapMessage.getVersion().getNamespace(), soapMessage
                    .getVersion().getAttrNameRole());
                if (role != null) {
                    role = role.trim();
                    if (role.equals(soapMessage.getVersion().getNextRole())
                        || role.equals(soapMessage.getVersion().getUltimateReceiverRole())) {
                        mustUnderstandHeaders.add(header);
                    } else {
                        for (URI roleFromBinding : serviceRoles) {
                            if (role.equals(roleFromBinding)) {
                                mustUnderstandHeaders.add(header);
                            }
                        }
                    }
                } else {
                    // if role omitted, the soap node is ultimate receiver,
                    // needs to understand
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
