package org.objectweb.celtix.systest.js;

import java.io.File;

import org.objectweb.celtix.js.rhino.ProviderFactory;

import org.objectweb.celtix.systest.common.TestServerBase;

public class Server extends TestServerBase {

    protected void run()  {
        try {
            ProviderFactory pf = new ProviderFactory();
            String f = getClass().getResource("resources/hello_world.js").getFile();
            pf.createAndPublish(new File(f), "http://localhost:9000/SoapContext/SoapPort", false);
            f = getClass().getResource("resources/hello_world.jsx").getFile();
            pf.createAndPublish(new File(f), "http://localhost:9100", false);
        } catch (Exception ex) {
            ex.printStackTrace();
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
}
