package org.objectweb.celtix.bus.jaxws.io;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;

import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPMessageDataWriter<T> implements DataWriter<T> {

    final DynamicDataBindingCallback callback;

    public SOAPMessageDataWriter(DynamicDataBindingCallback cb) {
        callback = cb;
    }

    public void write(Object obj, T output) {
        SOAPMessage dest = (SOAPMessage) output;
        try {
            if (DOMSource.class.isAssignableFrom(obj.getClass())) {
                DOMSource src = (DOMSource) obj;
                dest.getSOAPPart().setContent(src);
            }
        } catch (SOAPException se) {
            //TODO
        }
    }

    public void write(Object obj, QName elName, T output) {
        //Complete
    }

    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutbound, T output) {
        //Complete
    }

}
