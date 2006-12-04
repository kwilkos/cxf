/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.jca.cxf.handlers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.apache.cxf.Bus;
import org.apache.cxf.jca.core.resourceadapter.ResourceAdapterInternalException;
import org.apache.cxf.jca.cxf.CXFInvocationHandler;
import org.apache.cxf.jca.cxf.CXFInvocationHandlerData;
import org.apache.cxf.jca.cxf.CXFManagedConnection;


public class InvocationHandlerFactory {

    private static final Logger LOG = Logger.getLogger(InvocationHandlerFactory.class.getName());

    final Class[] handlerChainTypes;

    private final Bus bus;
    private final CXFManagedConnection managedConnection;

    public InvocationHandlerFactory(Bus b, CXFManagedConnection connection)
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

    public CXFInvocationHandler createHandlers(Object target, Subject subject)
        throws ResourceAdapterInternalException {

        CXFInvocationHandler first = null;
        CXFInvocationHandler last = null;

        // Create data member
        CXFInvocationHandlerData data = new CXFInvocationHandlerDataImpl();
        data.setBus(bus);
        data.setManagedConnection(managedConnection);
        data.setSubject(subject);
        data.setTarget(target);

        for (int i = 0; i < handlerChainTypes.length; i++) {
            CXFInvocationHandler newHandler;
            try {
                Constructor newHandlerConstructor = handlerChainTypes[i]
                    .getDeclaredConstructor(new Class[] {CXFInvocationHandlerData.class});
                newHandler = (CXFInvocationHandler)newHandlerConstructor.newInstance(new Object[] {data});
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

        String[] classNames = {"org.apache.cxf.jca.cxf.handlers.ProxyInvocationHandler",
                               "org.apache.cxf.jca.cxf.handlers.ObjectMethodInvocationHandler",
                               //"org.apache.cxf.jca.cxf.handlers.SecurityInvocationHandler",
                               //"org.apache.cxf.jca.cxf.handlers.TransactionHandler",
                               "org.apache.cxf.jca.cxf.handlers.InvokingInvocationHandler"};

        Class[] classes = new Class[classNames.length];

        for (int i = 0; i < classNames.length; i++) {
            LOG.fine("reading handler class: " + classNames[i]);
            classes[i] = getClass().getClassLoader().loadClass(classNames[i]);
        }
        return classes;
    }

}
