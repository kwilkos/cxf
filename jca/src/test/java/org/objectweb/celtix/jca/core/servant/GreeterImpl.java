package org.objectweb.celtix.jca.core.servant;

import java.rmi.RemoteException;
//import com.iona.mockobject.MockInvoke;

public class GreeterImpl implements Greeter { 
    private boolean sayHiCalled;
//    public MockInvoke mockInvoke;

    public GreeterImpl() {
        sayHiCalled = false;
//        MockInvoke mockInvoke = null;
    }

    public boolean getSayHiCalled() {
        return sayHiCalled;
    }
    public void setSayHiCalled(boolean b) {
        this.sayHiCalled = b;
    }
    public String sayHi() throws RemoteException {
        sayHiCalled = true;
/*
        if (mockInvoke instanceof MockInvoke) {
            try {
                return (String) ((MockInvoke)mockInvoke).invoke(null);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                }
                throw new RuntimeException("unexpected exception in test:" + t, t);
            }
        }
*/
        return "hi";
    } 
}
