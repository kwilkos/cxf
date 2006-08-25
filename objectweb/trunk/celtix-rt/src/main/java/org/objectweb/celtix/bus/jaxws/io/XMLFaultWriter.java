package org.objectweb.celtix.bus.jaxws.io;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;
import org.w3c.dom.*;

import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.bindings.xml.XMLFault;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.celtix.context.ObjectMessageContext;

public class XMLFaultWriter<T> implements DataWriter<T> {
    final JAXBDataBindingCallback callback;
    
    public XMLFaultWriter(JAXBDataBindingCallback cb) {
        callback = cb;
    }
    public void write(Object obj, T output) {
        WebFault wfAnnotation = obj.getClass().getAnnotation(WebFault.class);
        if (wfAnnotation != null) {
            QName elName = new QName(wfAnnotation.targetNamespace(), wfAnnotation.name());
            write(obj, elName, output);
        }
    }
    public void write(Object obj, QName elName, T output) {
        Object faultInfo = getFaultInfo((Throwable)obj);
        if (faultInfo != null) {
            XMLFault fault = (XMLFault)output;
            Node detail = fault.addFaultDetail();
            JAXBEncoderDecoder.marshall(callback.getJAXBContext(),
                callback.getSchema(), faultInfo, elName, detail);
            fault.setFaultDetail(detail);
        }
    }
    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutbound, T output) {
        throw new UnsupportedOperationException();
    }
    private Object getFaultInfo(Throwable fault) {
        try {
            Method faultInfoMethod = fault.getClass().getMethod("getFaultInfo");
            if (faultInfoMethod != null) {
                return faultInfoMethod.invoke(fault);
            }
        } catch (Exception ex) {
            throw new WebServiceException("Could not get faultInfo out of Exception", ex);
        }

        return null;
    }
}
