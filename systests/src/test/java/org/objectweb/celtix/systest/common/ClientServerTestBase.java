package org.objectweb.celtix.systest.common;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;

public abstract class ClientServerTestBase extends TestCase {
    
    static { 
        System.setProperty(ProviderImpl.JAXWSPROVIDER_PROPERTY, ProviderImpl.JAXWS_PROVIDER);
    }

    protected ClientServerTestBase() { 
    } 

    protected ClientServerTestBase(String name) { 
        super(name); 
    } 

}
