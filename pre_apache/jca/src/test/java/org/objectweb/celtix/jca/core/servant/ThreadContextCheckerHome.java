package org.objectweb.celtix.jca.core.servant;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

import junit.framework.TestCase;

public class ThreadContextCheckerHome implements EJBHome {
    final Object ejb;
    final ClassLoader cl;
    final TestCase test;

    public ThreadContextCheckerHome(Object ejbObj, ClassLoader cLoader, TestCase tCase) {
        this.ejb = ejbObj;
        this.cl = cLoader;
        this.test = tCase;
    }

    public Object create() throws RemoteException {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        TestCase.assertSame("thread context classloader is set as expected, current=" + current,
                            current, cl);
        return ejb;
    }
    
    // default impemenations
    public void remove(Handle handle) throws RemoteException, RemoveException {
        // do nothing here
    }
    
    public void remove(Object primaryKey) throws RemoteException, RemoveException {
        // do nothing here
    }
    
    public EJBMetaData getEJBMetaData() throws RemoteException {
        return null;
    }
    public HomeHandle getHomeHandle() throws RemoteException {
        return null;
    }
}
