package org.objectweb.celtix.bindings;

import org.objectweb.celtix.context.ObjectMessageContext;

public interface DataBindingCallbackFactory {

    DataBindingCallback createDataBindingCallback(ObjectMessageContext objContext,
                                                  DataBindingCallback.Mode mode);
    
}
