package org.objectweb.celtix.systest.routing;
import java.io.File;
import java.net.URL;

import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.routing.RouterManager;
import org.objectweb.celtix.systest.common.TestServerBase;
import org.objectweb.celtix.testutil.common.TestUtil;

public class RouterServer extends TestServerBase {
    private static final String CELTIX_ROUTER_TMP = new String("/celtix-router-tmp");
    private Bus bus;

    public RouterServer(String[] args) {
        URL routerConfigFileUrl = getClass().getResource("router_config.xml");
        System.setProperty("celtix.config.file", routerConfigFileUrl.toString());
        try {
            bus = Bus.init(args);
        } catch (BusException be) {
            throw new WebServiceException("Could not initialize bus", be);
        }
    }

    protected void run()  {
        RouterManager rm = new RouterManager(bus);
        rm.init();
    }

    private void cleanup() {
        TestUtil.deleteDir(new File(System.getProperty("user.dir"),
                                    CELTIX_ROUTER_TMP));
    }
    
    public boolean stopInProcess() throws Exception {
        System.clearProperty("celtix.config.file");
        tearDown();
        return super.stopInProcess();
    }

    //Called in context of start method
    public void tearDown() throws Exception {
        cleanup();
        super.tearDown();
    }

    public static void main(String[] args) {
        RouterServer s = null;
        try {
            s = new RouterServer(args);
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
