package org.objectweb.celtix.systest.routing;
import java.net.URL;

import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.routing.RouterManager;
import org.objectweb.celtix.systest.common.TestServerBase;

public class RouterServer extends TestServerBase {
    private Bus bus;

    public RouterServer(String[] args) {
        URL routerConfigFileUrl = getClass().getResource("router_config.xml");
        System.setProperty("celtix.config.file", routerConfigFileUrl.toString());
        //System.out.println("CF:" + routerConfigFileUrl.toString());
        try {
            bus = Bus.init(args);
        } catch (BusException be) {
            throw new WebServiceException("Could not initialize bus", be);
        }
    }

    protected void run()  {
        //RouterManager.main(serverArgs);
        RouterManager rm = new RouterManager(bus);        
        rm.init();
    }

    public boolean stopInProcess() throws Exception {
        System.clearProperty("celtix.config.file");
        return super.stopInProcess();
    }

    public static void main(String[] args) {
        try {
            RouterServer s = new RouterServer(args);
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
