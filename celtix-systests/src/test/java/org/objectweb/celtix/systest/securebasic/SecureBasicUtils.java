package org.objectweb.celtix.systest.securebasic;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

public class SecureBasicUtils extends ClientServerTestBase {
    
    private static final String BACK_TO_SRC_DIR = 
        "../../../../../../../src/test/java/org/objectweb/celtix/systest/securebasic/";
    
    public static boolean startServer(String configFileLocation,
                                         String beanId,
                                         String securityConfigurer,
                                         ClientServerSetupBase cssb,
                                         Class clazz) {
        
        Map<String, String> props = new HashMap<String, String>(); 
        
        if (configFileLocation != null) {
            props.put("celtix.config.file", configFileLocation);
        }
        if (securityConfigurer != null) {
            props.put(beanId, securityConfigurer);
        }
        assertTrue("server did not launch correctly", cssb.launchServer(clazz, props, null));
        return true;
    }
    
    protected static String getTestDir(Object obj) {
        return obj.getClass().getResource(".").getPath() + BACK_TO_SRC_DIR;
    }
    
 
    
}
