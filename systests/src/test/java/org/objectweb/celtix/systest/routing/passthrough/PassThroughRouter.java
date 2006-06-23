package org.objectweb.celtix.systest.routing.passthrough;

import org.objectweb.celtix.systest.routing.RouterServer;

public class PassThroughRouter extends RouterServer {
    public PassThroughRouter(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        PassThroughRouter s = null;
        try {
            s = new PassThroughRouter(args);
            s.start();
            //s.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
