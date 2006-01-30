package org.objectweb.celtix.bus.jaxws.io;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPBodyDataWriter<T> implements DataWriter<T> {

    final DynamicDataBindingCallback callback;

    public SOAPBodyDataWriter(DynamicDataBindingCallback cb) {
        callback = cb;
    }

    public void write(Object obj, T output) {
        SOAPBody dest = (SOAPBody)output;
        try {
            if (DOMSource.class.isAssignableFrom(obj.getClass())) {
                DOMSource domSource = (DOMSource)obj;
                dest.addDocument((Document)domSource.getNode());
            }
        } catch (SOAPException se) {
            se.printStackTrace();
        }
    }

    public void write(Object obj, QName elName, T output) {
        //Complete
    }

    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutbound, T output) {
        // Complete
    }

}
