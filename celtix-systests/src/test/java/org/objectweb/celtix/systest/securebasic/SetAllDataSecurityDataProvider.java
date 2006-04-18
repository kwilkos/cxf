package org.objectweb.celtix.systest.securebasic;

import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.bus.configuration.security.SSLServerPolicy;
//import org.objectweb.celtix.bus.transports.http.SecurityDataProvider; 


public class SetAllDataSecurityDataProvider {

    public static final long serialVersionUID = 1L;
    
    private static final String BACK_TO_SRC_DIR = 
        "../../../../../../../src/test/java/org/objectweb/celtix/systest/securebasic/";
        
    public SetAllDataSecurityDataProvider() {
        
    }
    
    public void configure(SSLServerPolicy sslPolicyParam) {
        sslPolicyParam.setKeystore(getClass().getResource(".").getPath() 
                                   + BACK_TO_SRC_DIR + ".clientkeystore");
        sslPolicyParam.setKeystoreType("JKS");
        sslPolicyParam.setKeystorePassword("clientpass");
        sslPolicyParam.setKeyPassword("clientpass");
        sslPolicyParam.setWantClientAuthentication(Boolean.TRUE);
        sslPolicyParam.setRequireClientAuthentication(Boolean.TRUE);
        sslPolicyParam.setTrustStore(getClass().getResource(".").getPath() 
                                     + BACK_TO_SRC_DIR
                                     + "truststore");
    }
    
    public void configure(SSLClientPolicy sslPolicyParam) {
        sslPolicyParam.setKeystore(getClass().getResource(".").getPath() 
                                   + BACK_TO_SRC_DIR + ".clientkeystore");
        sslPolicyParam.setKeystoreType("JKS");
        sslPolicyParam.setKeystorePassword("clientpass");
        sslPolicyParam.setKeyPassword("clientpass");
        sslPolicyParam.setTrustStore(getClass().getResource(".").getPath() 
                                   + BACK_TO_SRC_DIR + "truststore");
    }
    
    
}
