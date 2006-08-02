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

    public void handleMessage(SoapMessage soapMessage) {


        Set<Element> mustUnderstandHeaders = new HashSet<Element>();
        Set<URI> serviceRoles = new HashSet<URI>();
        Set<QName> notUnderstandQNames = new HashSet<QName>();
        Set<QName> mustUnderstandQNames = new HashSet<QName>();
        
        buildMustUnderstandHeaders(mustUnderstandHeaders, soapMessage, serviceRoles);
        initServiceSideInfo(mustUnderstandQNames, soapMessage, serviceRoles);
        if (!checkUnderstand(mustUnderstandHeaders, mustUnderstandQNames, notUnderstandQNames)) {
            StringBuffer sb = new StringBuffer(300);
            sb.append("Can't understands QNames: ");
            for (QName qname : notUnderstandQNames) {
                sb.append(qname.toString() + ", ");
            }
            SOAPException mustUnderstandException = new SOAPException(sb.toString());
            soapMessage.setContent(Exception.class, mustUnderstandException);
        }
    }

    private void initServiceSideInfo(Set<QName> mustUnderstandQNames, SoapMessage soapMessage,
                                     Set<URI> serviceRoles) {

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

    private void buildMustUnderstandHeaders(Set<Element> mustUnderstandHeaders, SoapMessage soapMessage,
                                            Set<URI> serviceRoles) {
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

    private boolean checkUnderstand(Set<Element> mustUnderstandHeaders, Set<QName> mustUnderstandQNames,
                                    Set<QName> notUnderstandQNames) {

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
