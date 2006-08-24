package org.objectweb.celtix.systest.common;

import junit.framework.TestCase;

import org.objectweb.celtix.jaxws.spi.ProviderImpl;


public abstract class ClientServerTestBase extends TestCase {
    
    static { 
        System.setProperty("javax.xml.ws.spi.Provider", ProviderImpl.class.getName());
    }

    protected ClientServerTestBase() { 
    } 

    protected ClientServerTestBase(String name) { 
        super(name); 
    } 

}
