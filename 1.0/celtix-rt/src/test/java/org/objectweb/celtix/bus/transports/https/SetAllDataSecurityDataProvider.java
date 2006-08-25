package org.objectweb.celtix.bus.transports.https;

import org.objectweb.celtix.bus.configuration.security.SSLClientPolicy;
import org.objectweb.celtix.bus.configuration.security.SSLServerPolicy;
//import org.objectweb.celtix.bus.transports.http.SecurityDataProvider; 


public class SetAllDataSecurityDataProvider {

    public static final long serialVersionUID = 1L;

        
    public SetAllDataSecurityDataProvider() {
        
    }
    
    public void configure(SSLServerPolicy sslPolicyParam) {
        sslPolicyParam.setKeystore(JettySslClientConfigurerTest.getPath("resources/defaultkeystore"));
        sslPolicyParam.setKeystoreType("JKS");
        sslPolicyParam.setKeystorePassword("defaultkeypass");
        sslPolicyParam.setKeyPassword("defaultkeypass");
        sslPolicyParam.setWantClientAuthentication(Boolean.TRUE);
        sslPolicyParam.setRequireClientAuthentication(Boolean.TRUE);
        sslPolicyParam.setTrustStore(JettySslClientConfigurerTest.getPath("resources/defaulttruststore"));
        sslPolicyParam.setTrustStoreType("MyType");
        sslPolicyParam.setSecureSocketProtocol("TLSv1");
        sslPolicyParam.getCiphersuites().add("MyCipher");
        sslPolicyParam.setSessionCaching(true);
        sslPolicyParam.setSessionCacheKey("sd");
        sslPolicyParam.setCertValidator("as");
        sslPolicyParam.setMaxChainLength(new Long(1));
    }
    
    public void configure(SSLClientPolicy sslPolicyParam) {
        sslPolicyParam.setKeystore(JettySslClientConfigurerTest.getPath("resources/defaultkeystore"));
        sslPolicyParam.setKeystoreType("JKS");
        sslPolicyParam.setKeystorePassword("defaultkeypass");
        sslPolicyParam.setKeyPassword("defaultkeypass");
        sslPolicyParam.setTrustStore(JettySslClientConfigurerTest.getPath("resources/defaulttruststore"));
        sslPolicyParam.setSecureSocketProtocol("TLSv1");
        sslPolicyParam.setSessionCaching(true);
        sslPolicyParam.setSessionCacheKey("sd");
        sslPolicyParam.setCertValidator("as");
        sslPolicyParam.setMaxChainLength(new Long(1));
    }
    
    
}
