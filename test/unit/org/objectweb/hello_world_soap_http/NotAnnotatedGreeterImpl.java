package org.objectweb.hello_world_soap_http;

import java.util.logging.Logger;

import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;
                
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

    public void testDocLitFault()  throws LiteralException {
        ErrorCode ec = new ErrorCode();
        ec.setMajor((short)1);
        ec.setMinor((short)1);
        NoSuchCodeLit nscl = new NoSuchCodeLit();
        nscl.setCode(ec);
        
        throw new LiteralException("TestException", nscl);
    }    
}
