package org.objectweb.celtix.systest.ws.addressing.jms;


import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.systest.ws.addressing.HeaderVerifier;
import org.objectweb.celtix.systest.ws.addressing.MAPVerifier;
import org.objectweb.celtix.systest.ws.addressing.VerificationCache;

public class Server extends TestServerBase implements VerificationCache {
    
    private String verified;
 
    protected void run()  {
        GreeterImpl implementor = new GreeterImpl();
        implementor.verificationCache = this;         
        String address = "jms:ConnectionFactory#dynamicQueues/test.jmstransport.addressing.queue";
        URL url = getClass().getResource("client.xml"); 
        assertNotNull("cannot find test resource", url);
        String configFileName = url.toString(); 
        if (configFileName != null) {
            System.setProperty("celtix.config.file", configFileName);
        }
        
        Endpoint endpoint = Endpoint.publish(address, implementor);
        List<Handler> handlerChain = endpoint.getBinding().getHandlerChain();
        for (Object h : handlerChain) {
            if (h instanceof MAPVerifier) {
                ((MAPVerifier)h).setVerificationCache(this);
            } else if (h instanceof HeaderVerifier) {
                ((HeaderVerifier)h).setVerificationCache(this);
            } 
        }
    }
    
    public static void main(String[] args) {
        try { 
            Server s = new Server(); 
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }

    public void put(String verification) {
        if (verification != null) {
            verified = verified == null
                       ? verification
                : verified + "; " + verification;
        }
    }

    /**
     * Used to facilitate assertions on server-side behaviour.
     *
     * @param log logger to use for diagnostics if assertions fail
     * @return true if assertions hold
     */
    protected boolean verify(Logger log) {
        if (verified != null) {
            log.log(Level.WARNING, 
                    "MAP/Header verification failed: {0}",
                    verified);
        }
        return verified == null;
    }
}
