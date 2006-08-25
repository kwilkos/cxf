package org.apache.cxf.transport.jms;

import java.util.Properties;

import javax.naming.Context;

import junit.framework.TestCase;

import org.apache.cxf.transports.jms.JMSAddressPolicyType;
import org.apache.cxf.transports.jms.JMSNamingPropertyType;

//import org.apache.cxf.transports.jms.JMSAddressPolicyType;
//import org.apache.cxf.transports.jms.JMSNamingPropertyType;

public class JMSUtilsTest extends TestCase {

    public JMSUtilsTest(String name) {
        super(name);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSUtilsTest.class);
    }
    
    public void setUp() throws Exception {
    }
    
    public void tearDown() throws Exception {
        //
    }
    
    // This is just a place holder for now it will be chaning in next task 
    // when the new JMS address policies and configurations are introdced.
    public void testpopulateIncomingContextNonNull() throws Exception {
        JMSAddressPolicyType addrType =  new JMSAddressPolicyType();
        
        JMSNamingPropertyType prop = new JMSNamingPropertyType();
        prop.setName(Context.APPLET);
        prop.setValue("testValue");
        addrType.getJMSNamingProperty().add(prop);      
        
        prop.setName(Context.BATCHSIZE);
        prop.setValue("12");
        addrType.getJMSNamingProperty().add(prop);
        
        
        Properties env = new Properties();
        assertTrue(env.size() <= 0);
        JMSUtils.populateContextEnvironment(addrType, env);
        assertTrue("Environment should not be empty", env.size() > 0);
        assertTrue("Environemnt should contain NamingBatchSize property", env.get(Context.BATCHSIZE) != null);
    }

}
