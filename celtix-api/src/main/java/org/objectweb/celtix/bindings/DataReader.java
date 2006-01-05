package org.objectweb.celtix.bindings;

import javax.xml.namespace.QName;

import org.objectweb.celtix.context.ObjectMessageContext;

public interface DataReader<T> {

    Object read(int idx, T input);
    Object read(QName name, int idx, T input);
    void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input);
}
