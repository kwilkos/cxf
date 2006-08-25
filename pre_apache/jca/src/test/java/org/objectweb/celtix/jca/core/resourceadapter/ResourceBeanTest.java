package org.objectweb.celtix.jca.core.resourceadapter;


import junit.framework.TestCase;

public class ResourceBeanTest extends TestCase {

    public ResourceBeanTest(String name) {
        super(name);
    }

 
    public void testDisableConsoleLogging() throws Exception {
        ResourceBean rb = new ResourceBean();
        rb.setDisableConsoleLogging(true);
    }
    

}
