package org.objectweb.celtix.bus.jaxws.io;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;

public class TestDynamicDataBindingCallback extends DynamicDataBindingCallback {

    private final Class<?>[] clazz;
    
    public TestDynamicDataBindingCallback(Class<?> cls, Mode md) {
        super(cls, md);
        clazz = new Class<?>[] {cls};
    }
    
    public Class<?>[] getSupportedFormats() {
        return clazz;
    }
    
}
