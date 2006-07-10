package org.objectweb.celtix.servicemodel;

import javax.xml.namespace.QName;


public interface DataWriter<T> {
    
    void write(Object obj, T output);
    void write(Object obj, QName elName, T output);

    /*
    //REVISIT
    void writeWrapper(ObjectMessageContext objCtx, boolean isOutbound, T output);
    */
}
