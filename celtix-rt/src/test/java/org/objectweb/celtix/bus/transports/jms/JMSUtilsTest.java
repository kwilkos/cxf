package org.objectweb.celtix.bus.transports.jms;

import java.util.Properties;

import javax.naming.Context;

import junit.framework.TestCase;

import org.objectweb.celtix.transports.jms.AddressType;

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
        AddressType addrType =  new AddressType();
        String prefix = "java.Naming";
        addrType.setJavaNamingApplet(prefix);
        addrType.setJavaNamingAuthoritative(prefix);
        addrType.setJavaNamingBatchsize("12");
        addrType.setJavaNamingDnsUrl(prefix);
        addrType.setJavaNamingFactoryInitial(prefix);
        addrType.setJavaNamingFactoryState(prefix);
        addrType.setJavaNamingFactoryUrlPkgs(prefix);
        addrType.setJavaNamingLanguage(prefix);
        addrType.setJavaNamingProviderUrl(prefix);
        addrType.setJavaNamingReferral(prefix);
        addrType.setJavaNamingSecurityAuthentication(prefix);
        addrType.setJavaNamingSecurityCredentials(prefix);
        addrType.setJavaNamingSecurityPrincipal(prefix);
        addrType.setJavaNamingSecurityProtocol(prefix);
        
        Properties env = new Properties();
        assertTrue(env.size() <= 0);
        JMSUtils.populateContextEnvironment(addrType, env);
        assertTrue("Environment should not be empty", env.size() > 0);
        assertTrue("Environemnt should contain NamingBatchSize property", env.get(Context.BATCHSIZE) != null);
    }

}
