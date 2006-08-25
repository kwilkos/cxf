package org.objectweb.celtix.systest.routing.bridge;

import org.objectweb.celtix.systest.routing.RouterServer;

public class NormalRouter extends RouterServer {
    public NormalRouter(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        NormalRouter s = null;
        try {
            s = new NormalRouter(args);
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
