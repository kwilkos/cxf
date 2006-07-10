package org.objectweb.celtix.jca.celtix;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.connector.Connection;
import org.objectweb.celtix.jca.celtix.handlers.InvocationHandlerFactory;
import org.objectweb.celtix.jca.core.resourceadapter.AbstractManagedConnectionImpl;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;


public class ManagedConnectionImpl 
    extends AbstractManagedConnectionImpl 
    implements CeltixManagedConnection, Connection {

    private static final Logger LOG = Logger.getLogger(ManagedConnectionImpl.class.getName());    

    private InvocationHandlerFactory handlerFactory;
    
    private Object celtixService;
    private boolean connectionHandleActive;

    public ManagedConnectionImpl(ManagedConnectionFactoryImpl managedFactory, ConnectionRequestInfo crInfo,
                                 Subject subject) throws ResourceException {
        super(managedFactory, crInfo, subject);
        LOG.fine("ManagedConnection created with hash: " + hashCode() + "." + toString());
    }

    public void associateConnection(Object arg0) throws ResourceException {
        try {           
            CeltixInvocationHandler handler = (CeltixInvocationHandler)Proxy
                .getInvocationHandler((Proxy)arg0);
            Object managedConnection = handler.getData().getManagedConnection();
            LOG.fine("previously associated managed connection: " + managedConnection.hashCode());

            if (managedConnection != this) {
                LOG.fine("associate handle " + arg0 + " with  managed connection " + this
                            + " with hashcode: " + hashCode());
                handler.getData().setManagedConnection(this);
                ((ManagedConnectionImpl)managedConnection).disassociateConnectionHandle(arg0);

                if (getCeltixService() == null) { 
                    // Very unlikely as THIS
                    // managed connection is
                    // already involved in a transaction.
                    celtixService = arg0;
                    connectionHandleActive = true;
                }

            }
        } catch (Exception ex) {         
            throw new ResourceAdapterInternalException("Error associating handle " + arg0
                                                       + " with managed connection " + this, ex);
        }
    }

    public CeltixManagedConnectionFactory getManagedConnectionFactory() {
        return (ManagedConnectionFactoryImpl)theManagedConnectionFactory();
    }

    final Object getCeltixService() {
        return celtixService;
    }

    final void initialiseCeltixService(ConnectionRequestInfo crInfo, Subject subject)
        throws ResourceException {
        LOG.fine("initialiseCeltixService, this=" + this + ", info=" + crInfo + ", subject=" + subject);

        this.crinfo = crInfo;
        this.subject = subject;

        celtixService = getCeltixServiceFromBus(subject, crInfo);
    }

    public Object getConnection(Subject subject, ConnectionRequestInfo crInfo) throws ResourceException {

        LOG.fine("getConnection: this=" + this + ", info=" + crInfo + ", subject=" + subject);
        Object connection = null;

        if (getCeltixService() == null) {
            initialiseCeltixService(crInfo, subject);
            connection = getCeltixService();
        } else {
            if (!connectionHandleActive && this.crinfo.equals(crInfo)) {
                connection = getCeltixService();
            } else {
                connection = getCeltixServiceFromBus(subject, crInfo);
            }
        }
        connectionHandleActive = true;
        return connection;
    }

    public synchronized Object getCeltixServiceFromBus(Subject subject, ConnectionRequestInfo crInfo)
        throws ResourceException {
        CeltixConnectionRequestInfo arReqInfo = (CeltixConnectionRequestInfo)crInfo;
        ClassLoader orig = Thread.currentThread().getContextClassLoader();

        Bus bus = getBus();

        Thread.currentThread().setContextClassLoader(bus.getClass().getClassLoader());

        QName serviceName = arReqInfo.getServiceQName();
        URL wsdlLocationUrl = arReqInfo.getWsdlLocationUrl();
        if (wsdlLocationUrl == null) { 
            // if the wsdlLocationUrl is null, set the default wsdl
            try {
                Object obj = null;
                Service service = Service.create(serviceName);
                obj = service.getPort(arReqInfo.getInterface());               
                setSubject(subject);
                return createConnectionProxy(obj, arReqInfo, subject);
            } catch (WebServiceException wse) {
                throw new ResourceAdapterInternalException("Failed to create proxy client for service "
                                                           + crInfo, wse);
            } finally {
                Thread.currentThread().setContextClassLoader(orig);
            }

        }

        try {
            Object obj = null;
            Service service = Service.create(wsdlLocationUrl, serviceName);
            if (arReqInfo.getPortQName() != null) {                                
                obj = service.getPort(arReqInfo.getPortQName(), arReqInfo.getInterface());
               
            } else {
                obj = service.getPort(arReqInfo.getInterface());
                //obj = bus.createClient(wsdlLocationUrl, serviceName, arReqInfo.getInterface());
            }

            setSubject(subject);
            return createConnectionProxy(obj, arReqInfo, subject);

        } catch (WebServiceException wse) {
            throw new ResourceAdapterInternalException("Failed to getPort for " + crInfo, wse);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        throw new NotSupportedException("Not Supported");
    }

    public boolean isBound() {
        return getCeltixService() != null;
    }

    
    // Compliance: WL9 checks
    // implemention of Connection method - never used as real Connection impl is
    // a java.lang.Proxy
    public void close() throws ResourceException {
        //TODO 
    }

    void disassociateConnectionHandle(Object handle) {
        if (celtixService == handle) {
            connectionHandleActive = false;
            celtixService = null;
        }
    }

    private Object createConnectionProxy(Object obj, CeltixConnectionRequestInfo cri, Subject subject)
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
        if (closingHandle == celtixService) {
            connectionHandleActive = false;
        }
        super.close(closingHandle);
    }

    // beging chucked from the pool
    public void destroy() throws ResourceException {
        connectionHandleActive = false;
        this.celtixService = null;
        super.destroy();
    }
   
    public CeltixTransaction getCeltixTransaction() {
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
