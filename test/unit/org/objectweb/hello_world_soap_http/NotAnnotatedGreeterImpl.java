package org.objectweb.hello_world_soap_http;

import java.util.logging.Logger;
                
public class NotAnnotatedGreeterImpl implements Greeter {

    private static final Logger LOG = 
        Logger.getLogger(NotAnnotatedGreeterImpl.class.getName());
    
    public String greetMe(String me) {
        LOG.info("Executing operation greetMe");
        return me;
    }
  
    public String sayHi() {
        LOG.info("Executing operation sayHi");
        return "Bonjour";
    }

}
