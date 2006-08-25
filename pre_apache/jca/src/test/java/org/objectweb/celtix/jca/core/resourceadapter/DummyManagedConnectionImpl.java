
package org.objectweb.celtix.jca.core.resourceadapter;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.security.auth.Subject;

import org.objectweb.celtix.jca.celtix.CeltixManagedConnectionFactory;


public class DummyManagedConnectionImpl extends AbstractManagedConnectionImpl {
    boolean bound;
    Object con;

    public DummyManagedConnectionImpl(
        AbstractManagedConnectionFactoryImpl managedFactory,
        ConnectionRequestInfo crInfo, Subject subject)
        throws ResourceException {
        super(managedFactory, crInfo, subject);
    }

    public void associateConnection(Object arg0) throws ResourceException {
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    public javax.transaction.xa.XAResource getXAResource()
        throws ResourceException {
        return null;
    }

    public Object getConnection(Subject subject, ConnectionRequestInfo crInfo)
        throws ResourceException {
        return con;
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean b) {
        bound = b;
    }

    // use to indicate invalid
    public void setCon(Object o) {
        con = o;
    }

    public CeltixManagedConnectionFactory getManagedConnectionFactory() { 
        return (CeltixManagedConnectionFactory)theManagedConnectionFactory();
    } 
}
