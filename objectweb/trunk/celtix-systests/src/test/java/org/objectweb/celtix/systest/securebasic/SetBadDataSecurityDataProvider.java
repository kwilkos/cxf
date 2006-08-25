package org.objectweb.celtix.systest.securebasic;

import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.bus.configuration.security.SSLServerPolicy;
//import org.objectweb.celtix.bus.transports.http.SecurityDataProvider;


public class SetBadDataSecurityDataProvider { 

    public static final long serialVersionUID = 1L;
    private static final String BACK_TO_SRC_DIR = 
        "../../../../../../../src/test/java/org/objectweb/celtix/systest/securebasic/";
        
    public SetBadDataSecurityDataProvider() {
        
    }
    
    public void configure(SSLServerPolicy sslPolicyParam) {
        sslPolicyParam.setTrustStore(getClass().getResource(".").getPath() 
                                     + BACK_TO_SRC_DIR
                                     + "defaulttruststore");
    }
    
    public void configure(SSLClientPolicy sslPolicyParam) {
        sslPolicyParam.setTrustStore(getClass().getResource(".").getPath() 
                                     + BACK_TO_SRC_DIR
                                     + "defaulttruststore");

    }
}
