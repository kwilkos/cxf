package org.objectweb.celtix.bus.jaxws.io;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.celtix.context.ObjectMessageContext;

public class NodeDataWriter<T> implements DataWriter<T> {
    final JAXBDataBindingCallback callback;
    
    public NodeDataWriter(JAXBDataBindingCallback cb) {
        callback = cb;
    }
    public void write(Object obj, T output) {
        write(obj, null, output);
    }
    public void write(Object obj, QName elName, T output) {
        JAXBEncoderDecoder.marshall(callback.getJAXBContext(), obj, elName, (Node)output);
    }
    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutBound, T output) {
        Object obj = callback.createWrapperType(objCtx, isOutBound);
        QName elName = isOutBound ? callback.getResponseWrapperQName()
            : callback.getRequestWrapperQName();
        write(obj, elName, output);
    }
}
