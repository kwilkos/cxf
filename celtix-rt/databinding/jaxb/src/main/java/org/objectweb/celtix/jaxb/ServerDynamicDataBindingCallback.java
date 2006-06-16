package org.objectweb.celtix.jaxb;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Provider;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.ServerDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

public class ServerDynamicDataBindingCallback<T> extends DynamicDataBindingCallback
    implements ServerDataBindingCallback {
    
    private Provider<T> provider;
    
    
    public ServerDynamicDataBindingCallback(Class<T> cls, Mode md, Provider<T> p) {
        super(cls, md);
        provider = p;
    }
    
    public ServerDynamicDataBindingCallback(JAXBContext ctx, Mode md, Provider<T> p) {
        super(ctx, md);
        provider = p;
    }
    

    @SuppressWarnings("unchecked")
    public void invoke(ObjectMessageContext octx) throws InvocationTargetException {
        T obj = provider.invoke((T)octx.getMessageObjects()[0]);
        octx.setReturn(obj);
    }
}
