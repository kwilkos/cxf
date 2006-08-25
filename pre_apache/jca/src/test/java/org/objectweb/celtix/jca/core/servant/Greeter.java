package org.objectweb.celtix.jca.core.servant;

public interface Greeter extends java.rmi.Remote {
    String sayHi() throws java.rmi.RemoteException;
} 
