package org.objectweb.celtix.bindings.soap2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.namespace.QName;

import org.objectweb.celtix.helpers.CastUtils;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;

public final class HeaderUtil {
    private static final String HEADERS_PROPERTY = HeaderUtil.class.getName() + ".HEADERS";

    private HeaderUtil() {
    }

    private static Set<QName> getHeaderParts(BindingMessageInfo bmi) {
        Object obj = bmi.getProperty(HEADERS_PROPERTY);
        if (obj == null) {
            Set<QName> set = new HashSet<QName>();
            List<MessagePartInfo> mps = bmi.getMessageInfo().getMessageParts();
            for (SOAPHeader head : bmi.getExtensors(SOAPHeader.class)) {
                String pn = head.getPart();
                for (MessagePartInfo mpi : mps) {
                    if (pn.equals(mpi.getName().getLocalPart())) {
                        if (mpi.isElement()) {
                            set.add(mpi.getElementQName());
                        } else {
                            set.add(mpi.getTypeQName());
                        }
                        break;
                    }
                }
            }
            bmi.setProperty(HEADERS_PROPERTY, set);
            return set;
        }
        return CastUtils.cast((Set<?>)obj);
    }

    public static Set<QName> getHeaderQNameInOperationParam(SoapMessage soapMessage) {
        Set<QName> headers = new HashSet<QName>();
        BindingOperationInfo bop = (BindingOperationInfo)soapMessage.getExchange()
            .get(BindingOperationInfo.class.getName());
        if (bop != null) {
            headers.addAll(getHeaderParts(bop.getInput()));
            headers.addAll(getHeaderParts(bop.getOutput()));
        }
        return headers;
    }
}
