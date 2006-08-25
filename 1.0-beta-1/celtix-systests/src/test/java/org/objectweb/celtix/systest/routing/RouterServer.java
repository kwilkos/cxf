package org.objectweb.celtix.systest.routing;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.routing.RouterManager;
import org.objectweb.celtix.systest.common.TestServerBase;

public class RouterServer extends TestServerBase {
    private static final String BUSID_PROPERTY = "org.objectweb.celtix.BusId";

    private Bus bus;

    public RouterServer(String[] args) {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx].equals(BUSID_PROPERTY)
                && (idx + 1 < args.length)) {
                properties.put(BUSID_PROPERTY, args[idx + 1]);
            }
        }

        URL routerConfigFileUrl = getClass().getResource("router_config.xml");
        System.setProperty("celtix.config.file", routerConfigFileUrl.toString());
        //System.out.println("CF:" + routerConfigFileUrl.toString());
        //System.out.println("BUSID:" + properties.get(BUSID_PROPERTY));
        try {
            bus = Bus.init(args, properties);
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
