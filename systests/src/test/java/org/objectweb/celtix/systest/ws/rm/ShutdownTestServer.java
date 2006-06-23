package org.objectweb.celtix.systest.ws.rm;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.systest.common.TestServerBase;

public class ShutdownTestServer extends TestServerBase {

    protected void run() {

        ControlImpl.setConfigFileProperty("oneway-terminate-on-shutdown");
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        builder.clearConfigurations();
        
        GreeterImpl implementor = new GreeterImpl();
        String address = "http://localhost:9020/SoapContext/GreeterPort";
        Endpoint.publish(address, implementor);

    }

    public static void main(String[] args) {
        try {
            ShutdownTestServer s = new ShutdownTestServer();
            s.start();            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
