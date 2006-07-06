package org.objectweb.celtix.jca.celtix.handlers;


import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;
import org.objectweb.celtix.jca.celtix.ManagedConnectionFactoryImpl;

/**
 * The object returned to the application
 * implement close and equals for the proxy
 */
public class ProxyInvocationHandler extends CeltixInvocationHandlerBase  {

    private static final Logger LOG = Logger.getLogger(ProxyInvocationHandler.class.getName());
    
    public ProxyInvocationHandler(CeltixInvocationHandlerData data) {
        super(data);
        LOG.fine("ProxyInvocationHandler instance created"); 
    }


    public final Object invoke(final Object proxy,
                               final Method method,
                               final Object args[]) throws Throwable {
       
        LOG.fine(this + " on " + method);
        Object o = getData().getManagedConnection().getManagedConnectionFactory();
        ManagedConnectionFactoryImpl mcf = (ManagedConnectionFactoryImpl)o;
        Bus.setCurrent(mcf.getBus());
        return invokeNext(proxy, method, args);
    }
}
