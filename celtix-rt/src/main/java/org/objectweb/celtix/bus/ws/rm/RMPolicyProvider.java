package org.objectweb.celtix.bus.ws.rm;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class RMPolicyProvider implements ConfigurationProvider {
    
    private static final Logger LOG = LogUtils.getL7dLogger(RMPolicyProvider.class);
    private Port port;
    private Definition def;

    public RMPolicyProvider(Bus b, EndpointReferenceType epr) {
        try {
            port = EndpointReferenceUtils.getPort(b.getWSDLManager(), epr);
            def = EndpointReferenceUtils.getWSDLDefinition(b.getWSDLManager(), epr);
        } catch (WSDLException ex) {
            LOG.log(Level.SEVERE, "POLICY_PROVIDER_CREATION_EXC", ex);
        }
    }

    public void init(Configuration configuration) {
        // not needed
    }

    public Object getObject(String name) {
        if (!"rmAssertion".equals(name)) {
            return null;
        }

        Element policyElem = getPolicy(port.getBinding().getExtensibilityElements(), null);
        if (null == policyElem) {
            policyElem = getPolicy(port.getExtensibilityElements(), null);
        }
        if (null != policyElem) {
            return getRMAssertion(policyElem);
        }

        return null;

    }

    private Element getPolicy(List<?> extensibilityElements, String name) {
        for (Object ep : extensibilityElements) {            
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if (ext instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement uExt = (UnknownExtensibilityElement)ext;
                if (RMUtils.getPolicyConstants().getPolicyQName().equals(ext.getElementType())) {
                    Element elem = uExt.getElement();                    
                    String id = elem.getAttributeNS(RMUtils.getPolicyConstants().getWSUNamespaceURI(), "Id");
                    if (null == name || name.equals(id)) {
                        return elem;
                    }
                } else if (RMUtils.getPolicyConstants().getPolicyReferenceQName()
                    .equals(ext.getElementType())) {
                    Element elem = uExt.getElement();
                    String uri = elem.getAttribute("URI");
                    if (uri.startsWith("#") && uri.length() > 1) {
                        Element referenced = getPolicy(def.getExtensibilityElements(), uri.substring(1));
                        if (null != referenced) {
                            return referenced;
                        }
                    } else {
                        LOG.log(Level.SEVERE, "POLICY_REFERENCE_RESOLUTION_EXC", uri);
                    }
                }
            }
        }
        return null;
    }

    private RMAssertionType getRMAssertion(Element policyElement) {
        RMAssertionType rma = null;
        NodeList nl = policyElement.getElementsByTagNameNS(RMUtils.getRMConstants().getRMPolicyNamespaceURI(),
                                                           "RMAssertion");
        if (nl.getLength() > 0) {
            JAXBContext context = null;
            String packageName = RMUtils.getWSRMPolicyFactory().getClass().getPackage().getName();
            try {
                context = JAXBContext.newInstance(packageName, getClass().getClassLoader());
                Unmarshaller u = context.createUnmarshaller();
                Object obj = u.unmarshal(nl.item(0));
                if (obj instanceof JAXBElement<?>) {
                    JAXBElement<?> el = (JAXBElement<?>)obj;
                    obj = el.getValue();
                }
                rma = (RMAssertionType)obj;
            } catch (JAXBException ex) {
                LOG.log(Level.SEVERE, "RMASSERTION_UNMARSHAL_EXC", ex);
            }
        }
        return rma;
    }

    public boolean setObject(String name, Object value) {
        return false;
    }

}
