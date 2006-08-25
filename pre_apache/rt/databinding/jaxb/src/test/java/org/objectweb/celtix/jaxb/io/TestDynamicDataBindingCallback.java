package org.objectweb.celtix.jaxb.io;

import javax.xml.bind.JAXBContext;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.jaxb.DynamicDataBindingCallback;

public class TestDynamicDataBindingCallback extends DynamicDataBindingCallback {

    private final Class<?>[] clazz;
    private final JAXBContext context;
    
    public TestDynamicDataBindingCallback(Class<?> cls, Mode md) {
        super(cls, md);
        clazz = new Class<?>[] {cls};
        context = null;
    }
    
    public TestDynamicDataBindingCallback(JAXBContext ctx, Mode md) {
        super(ctx, md);
        context = ctx;
        clazz = new Class<?>[] {Object.class};
    }
    
    public Class<?>[] getSupportedFormats() {
        return clazz;
    }
    
    public JAXBContext getJAXBContext() {
        return context;
    }
    
}
