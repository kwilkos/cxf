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
package org.apache.cxf.jca.cxf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
//import java.util.Iterator;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
//import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.connector.Connection;
import org.apache.cxf.jca.core.resourceadapter.AbstractManagedConnectionImpl;
import org.apache.cxf.jca.core.resourceadapter.ResourceAdapterInternalException;
import org.apache.cxf.jca.cxf.handlers.InvocationHandlerFactory;

public class ManagedConnectionImpl 
    extends AbstractManagedConnectionImpl 
    implements CXFManagedConnection, Connection {

    private static final Logger LOG = LogUtils.getL7dLogger(ManagedConnectionImpl.class);    

    private InvocationHandlerFactory handlerFactory;
    
    private Object cxfService;
    private boolean connectionHandleActive;

    public ManagedConnectionImpl(ManagedConnectionFactoryImpl managedFactory, ConnectionRequestInfo crInfo,
                                 Subject subject) throws ResourceException {
        super(managedFactory, crInfo, subject);
        LOG.fine("ManagedConnection created with hash: " + hashCode() + "." + toString());
    }

    public void associateConnection(Object arg0) throws ResourceException {
        try {           
            CXFInvocationHandler handler = (CXFInvocationHandler)Proxy.getInvocationHandler((Proxy)arg0);
            Object managedConnection = handler.getData().getManagedConnection();

            if (managedConnection != this) {
                handler.getData().setManagedConnection(this);
                ((ManagedConnectionImpl)managedConnection).disassociateConnectionHandle(arg0);

                if (getCXFService() == null) { 
                    // Very unlikely as THIS
                    // managed connection is
                    // already involved in a transaction.
                    cxfService = arg0;
                    connectionHandleActive = true;
                }

            }
        } catch (Exception ex) {         
            throw new ResourceAdapterInternalException("Error associating handle " + arg0
                                                       + " with managed connection " + this, ex);
        }
    }

    public CXFManagedConnectionFactory getManagedConnectionFactory() {
        return (ManagedConnectionFactoryImpl)theManagedConnectionFactory();
    }

    final Object getCXFService() {
        return cxfService;
    }

    final void initialiseCXFService(ConnectionRequestInfo crInfo, Subject subject)
        throws ResourceException {
        this.crinfo = crInfo;
        this.subject = subject;

        cxfService = getCXFServiceFromBus(subject, crInfo);
    }

    public Object getConnection(Subject subject, ConnectionRequestInfo crInfo) throws ResourceException {

        Object connection = null;

        if (getCXFService() == null) {
            initialiseCXFService(crInfo, subject);
            connection = getCXFService();
            
        } else {
            if (!connectionHandleActive && this.crinfo.equals(crInfo)) {
                connection = getCXFService();
            } else {
                connection = getCXFServiceFromBus(subject, crInfo);
            }
        }
        connectionHandleActive = true;
        return connection;
    }

    public synchronized Object getCXFServiceFromBus(Subject subject, ConnectionRequestInfo crInfo)
        throws ResourceException {

        CXFConnectionRequestInfo arReqInfo = (CXFConnectionRequestInfo)crInfo;
        
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        QName serviceName = arReqInfo.getServiceQName();
        URL wsdlLocationUrl = arReqInfo.getWsdlLocationUrl();
        
        try {
            Object obj = null;
            Service service = null;
            if (wsdlLocationUrl == null) {    
                service = Service.create(serviceName);
            } else {
                service = Service.create(wsdlLocationUrl, serviceName);
            }
        
            QName port = arReqInfo.getPortQName();
            if (port == null) {
                obj = service.getPort(arReqInfo.getInterface());
            } else {
                obj = service.getPort(arReqInfo.getPortQName(), arReqInfo.getInterface());
            }
            
            setSubject(subject);
            
            return createConnectionProxy(obj, arReqInfo, subject);
        } catch (WebServiceException wse) {
            throw new ResourceAdapterInternalException("Failed to create proxy client for service " 
                                                       + arReqInfo, wse);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }

 
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        throw new NotSupportedException("Not Supported");
    }

    public boolean isBound() {
        return getCXFService() != null;
    }

    
    // Compliance: WL9 checks
    // implemention of Connection method - never used as real Connection impl is
    // a java.lang.Proxy
    public void close() throws ResourceException {
        //TODO 
    }

    void disassociateConnectionHandle(Object handle) {
        if (cxfService == handle) {
            connectionHandleActive = false;
            cxfService = null;
        }
    }

    private Object createConnectionProxy(Object obj, CXFConnectionRequestInfo cri, Subject subject)
        throws ResourceException {

        Class classes[] = {Connection.class, cri.getInterface()};

        return Proxy.newProxyInstance(cri.getInterface().getClassLoader(), classes, 
                                      createInvocationHandler(obj, subject));
    }

    private InvocationHandler createInvocationHandler(Object obj, Subject subject) throws ResourceException {

        return getHandlerFactory().createHandlers(obj, subject);
    }

    private InvocationHandlerFactory getHandlerFactory() throws ResourceException {
        if (handlerFactory == null) {
            handlerFactory = new InvocationHandlerFactory(getBus(), this);
        }
        return handlerFactory;
    }

    private Bus getBus() {
        return ((ManagedConnectionFactoryImpl)getManagedConnectionFactory()).getBus();
    }

    
    public void close(Object closingHandle) throws ResourceException {
        if (closingHandle == cxfService) {
            connectionHandleActive = false;
        }
        super.close(closingHandle);
    }

    // beging chucked from the pool
    public void destroy() throws ResourceException {
        connectionHandleActive = false;
        this.cxfService = null;
        super.destroy();
    }
   
    public CXFTransaction getCXFTransaction() {
        //TODO should throw the exception  
        return null;
    }

    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException();
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException();
    }
}
