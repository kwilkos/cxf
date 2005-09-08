package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.logging.Logger;
                
public class NotAnnotatedGreeterImpl implements Greeter {

    private static final Logger LOG = 
        Logger.getLogger(NotAnnotatedGreeterImpl.class.getName());
    
    public String greetMe(String me) throws RemoteException {
        LOG.info("Executing operation sayHi");
        return null;
    }
  
    public String sayHi() throws RemoteException {
        LOG.info("Executing operation greetMe");
        return "Bonjour";
    }

}
