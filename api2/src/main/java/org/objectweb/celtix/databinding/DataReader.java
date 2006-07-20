package org.objectweb.celtix.databinding;

import javax.xml.namespace.QName;


public interface DataReader<T> {

    Object read(int idx, T input);
    Object read(QName name, int idx, T input);

    /*
    //REVISIT
    void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input);
    */

}
