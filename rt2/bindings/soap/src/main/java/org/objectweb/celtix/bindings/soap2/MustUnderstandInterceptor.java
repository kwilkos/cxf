package org.objectweb.celtix.bindings.soap2;

import java.net.URI;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.phase.Phase;

public class MustUnderstandInterceptor extends AbstractSoapInterceptor {
    private static final Logger LOG = LogUtils.getL7dLogger(MustUnderstandInterceptor.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    public MustUnderstandInterceptor() {
        super();
        setPhase(Phase.PROTOCOL);
    }

    public void handleMessage(SoapMessage soapMessage) {
        Set<Element> mustUnderstandHeaders = new HashSet<Element>();
        Set<URI> serviceRoles = new HashSet<URI>();
        Set<QName> notUnderstandQNames = new HashSet<QName>();
        Set<QName> mustUnderstandQNames = new HashSet<QName>();

        buildMustUnderstandHeaders(mustUnderstandHeaders, soapMessage, serviceRoles);
        initServiceSideInfo(mustUnderstandQNames, soapMessage, serviceRoles);
        if (!checkUnderstand(mustUnderstandHeaders, mustUnderstandQNames, notUnderstandQNames)) {
            StringBuffer sb = new StringBuffer(300);
            int pos = 0;
            for (QName qname : notUnderstandQNames) {
                pos = pos + qname.toString().length() + 2;
                sb.append(qname.toString() + ", ");
            }
            sb.delete(pos - 2, pos);
            throw new SoapFault(new Message("MUST_UNDERSTAND", BUNDLE, sb.toString()),
                                SoapFault.MUST_UNDERSTAND);
        }
    }

    private void initServiceSideInfo(Set<QName> mustUnderstandQNames, SoapMessage soapMessage,
                                     Set<URI> serviceRoles) {
        // 
        // Set<QName> paramHeaders =
        // HeaderUtil.getHeaderQNameInOperationParam(soapMessage);
        // if (paramHeaders != null) {
        // mustUnderstandQNames.addAll(paramHeaders);
        // }
        for (Interceptor interceptorInstance : soapMessage.getInterceptorChain()) {
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
            if (headers.getChildNodes().item(i) instanceof Element) {
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
