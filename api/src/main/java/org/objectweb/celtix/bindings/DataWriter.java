package org.objectweb.celtix.bindings;

import javax.xml.namespace.QName;

import org.objectweb.celtix.context.ObjectMessageContext;

public interface DataWriter<T> {
    
    void write(Object obj, T output);
    void write(Object obj, QName elName, T output);
    void writeWrapper(ObjectMessageContext objCtx, boolean isOutbound, T output);
}
