package org.objectweb.celtix.bindings;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.celtix.context.ObjectMessageContext;

public interface ServerDataBindingCallback extends DataBindingCallback {

    
    
    void invoke(ObjectMessageContext octx) throws InvocationTargetException;
}
