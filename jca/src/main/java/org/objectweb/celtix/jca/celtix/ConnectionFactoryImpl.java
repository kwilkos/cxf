package org.objectweb.celtix.jca.celtix;


import java.io.Serializable;
import java.net.URL;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.xml.namespace.QName;


import org.objectweb.celtix.connector.CeltixConnectionFactory;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;

public class ConnectionFactoryImpl implements CeltixConnectionFactory, 
                                              Referenceable, 
                                              Serializable {
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryImpl.class.getName());
    private ManagedConnectionFactory managedConnectionFactory;
    private ConnectionManager connectionManager;
    private Reference reference;

    public ConnectionFactoryImpl(ManagedConnectionFactory aMCF, ConnectionManager aCM) {
        managedConnectionFactory = aMCF;
        connectionManager = aCM;
        LOG.info("this=" + this);
    }

    public void setReference(Reference ref) {
        LOG.info("Reference : " + ref + " is set");
        reference = ref;
    }

    public Reference getReference() throws NamingException {
        LOG.info("Reference : " + reference + " is returned");
        return reference;
    }
    
    public Object getConnection(Class iface, URL wsdlLocation, QName serviceName) throws ResourceException {
        return getConnection(iface, wsdlLocation, serviceName, null);
    }

    public Object getConnection(Class iface, QName serviceName, QName portName) throws ResourceException {
        return getConnection(iface, null, serviceName, portName);
    }

    public Object getConnection(Class iface, QName serviceName) throws ResourceException {
        return getConnection(iface, null, serviceName, null);
    }
  

    public Object getBus() { 
        return ((ManagedConnectionFactoryImpl)managedConnectionFactory).getBus();
    }

    public Object getConnection(Class iface, URL wsdlLocation, QName serviceName, QName portName)
        throws ResourceException {
        
        if (!iface.isInterface()) {
            throw new ResourceAdapterInternalException(
                    "The first argument to getConnection must be an Interface",
                    new IllegalArgumentException(iface.toString() + " is not an Interface."));
        }

        LOG.info("connecting to: " + iface);

        CeltixConnectionRequestInfo reqInfo = 
            new CeltixConnectionRequestInfo(iface, wsdlLocation, serviceName, portName);

        if (connectionManager == null) {
            // non-managed, null Subject
            ManagedConnection connection = managedConnectionFactory.createManagedConnection(null, reqInfo);
            return connection.getConnection(null, reqInfo);
        } else {
            return connectionManager.allocateConnection(managedConnectionFactory, reqInfo);
        }
    }
}

















