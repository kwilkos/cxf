package org.objectweb.celtix.axisinterop;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.celtix.testutil.common.AbstractTestServerBase;

public class CeltixServer extends AbstractTestServerBase {
    
    static {
        System.setProperty(ProviderImpl.JAXWSPROVIDER_PROPERTY, ProviderImpl.JAXWS_PROVIDER);
    }

    protected void run()  {
        Object implementor = new CeltixEchoImpl();
        String address = "http://localhost:9240/CeltixEchoService/Echo";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String[] args) {
        try { 
            CeltixServer s = new CeltixServer(); 
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
