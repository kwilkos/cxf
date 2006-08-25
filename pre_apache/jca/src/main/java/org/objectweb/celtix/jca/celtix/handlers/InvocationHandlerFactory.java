package org.objectweb.celtix.jca.celtix.handlers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;
import org.objectweb.celtix.jca.celtix.CeltixManagedConnection;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;

public class InvocationHandlerFactory {

    private static final Logger LOG = Logger.getLogger(InvocationHandlerFactory.class.getName());

    final Class[] handlerChainTypes;

    private final Bus bus;
    private final CeltixManagedConnection managedConnection;

    public InvocationHandlerFactory(Bus b, CeltixManagedConnection connection)
        throws ResourceAdapterInternalException {

        this.bus = b;
        this.managedConnection = connection;

        try {
            handlerChainTypes = getHandlerChainDefinition();
        } catch (Exception ex) {
            ResourceAdapterInternalException raie = new ResourceAdapterInternalException(
                                                           "unable to load handler chain definition",
                                                           ex);
            LOG.warning(ex.getMessage());
            throw raie;
        }
    }

    public CeltixInvocationHandler createHandlers(Object target, Subject subject)
        throws ResourceAdapterInternalException {

        CeltixInvocationHandler first = null;
        CeltixInvocationHandler last = null;

        // Create data member
        CeltixInvocationHandlerData data = new CeltixInvocationHandlerDataImpl();
        data.setBus(bus);
        data.setManagedConnection(managedConnection);
        data.setSubject(subject);
        data.setTarget(target);

        for (int i = 0; i < handlerChainTypes.length; i++) {
            CeltixInvocationHandler newHandler;
            try {
                Constructor newHandlerConstructor = handlerChainTypes[i]
                    .getDeclaredConstructor(new Class[] {CeltixInvocationHandlerData.class});
                newHandler = (CeltixInvocationHandler)newHandlerConstructor.newInstance(new Object[] {data});
            } catch (Exception ex) {
                ResourceAdapterInternalException raie = new ResourceAdapterInternalException(
                                                           "error creating InvocationHandler: "
                                                           + handlerChainTypes[i],
                                                           ex);
                LOG.warning(raie.getMessage());
                throw raie;
            }

            if (last != null) {
                last.setNext(newHandler);
                last = newHandler;
            } else {
                first = newHandler;
                last = newHandler;
            }
        }
        return first;
    }

    private Class[] getHandlerChainDefinition() throws IOException, ClassNotFoundException {

        String[] classNames = {"org.objectweb.celtix.jca.celtix.handlers.ProxyInvocationHandler",
                               "org.objectweb.celtix.jca.celtix.handlers.ObjectMethodInvocationHandler",
                               //"org.objectweb.celtix.jca.celtix.handlers.SecurityInvocationHandler",
                               //"org.objectweb.celtix.jca.celtix.handlers.TransactionHandler",
                               "org.objectweb.celtix.jca.celtix.handlers.InvokingInvocationHandler"};

        Class[] classes = new Class[classNames.length];

        for (int i = 0; i < classNames.length; i++) {
            LOG.fine("reading handler class: " + classNames[i]);
            classes[i] = getClass().getClassLoader().loadClass(classNames[i]);
        }
        return classes;
    }

}
