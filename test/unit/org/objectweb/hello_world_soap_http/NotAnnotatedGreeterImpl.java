package org.objectweb.hello_world_soap_http;

import java.rmi.RemoteException;
import java.util.logging.Logger;
                
public class NotAnnotatedGreeterImpl implements Greeter {

    private static Logger logger = 
        Logger.getLogger(NotAnnotatedGreeterImpl.class.getPackage().getName());
    
    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMe(java.lang.String)
     */
    
    public String greetMe(String me) throws RemoteException {
        logger.info("Executing operation sayHi");
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#sayHi()
     */
  
    public String sayHi() throws RemoteException {
        logger.info("Executing operation greetMe");
        return "Bonjour";
    }

}
