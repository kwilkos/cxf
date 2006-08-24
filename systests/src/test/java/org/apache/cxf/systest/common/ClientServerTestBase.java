package org.apache.cxf.systest.common;

import junit.framework.TestCase;

import org.apache.cxf.jaxws.spi.ProviderImpl;


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
