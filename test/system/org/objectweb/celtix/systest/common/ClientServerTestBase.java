package org.objectweb.celtix.systest.common;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;

public abstract class ClientServerTestBase extends TestCase {
    
    static { 
        System.setProperty(ProviderImpl.JAXWSPROVIDER_PROPERTY, ProviderImpl.JAXWS_PROVIDER);
    }
    
    protected static int setUpCount; 
    private static File onetimeMarker;
    private static Bus bus; 

    private final List<ServerLauncher> launchers = new ArrayList<ServerLauncher>();  

    protected ClientServerTestBase() { 
    } 

    protected ClientServerTestBase(String name) { 
        super(name); 
    } 

    public void setUp() throws BusException {    
        
        if (!oneTimeSetUpDone()) { 
            initBus();
            onetimeSetUp();
            oneTimeSetUpComplete();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() { 
                        stopAllServers();
                    }
                });
        }
        setUpCount++;
    }
    

    private void oneTimeSetUpComplete() { 
        try {
            onetimeMarker = File.createTempFile("clientservertest", ".lock");
            onetimeMarker.deleteOnExit(); 
            onetimeMarker.createNewFile();
            assertTrue(onetimeMarker.exists()); 

        } catch (IOException e) {
            fail(e.toString()); 
        }
    } 

    private boolean oneTimeSetUpDone() { 
        return onetimeMarker == null ? false : onetimeMarker.exists();
    } 



    protected void initBus() throws BusException { 
        bus = Bus.init();
    }
    
    protected Bus getBus() {
        return bus;
    }

    protected void onetimeSetUp() throws BusException { 
        // emtpy
    } 

    protected boolean stopAllServers() {
        boolean passed = true;
        for (ServerLauncher sl : launchers) {
            try { 
                passed = passed && sl.stopServer(); 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        launchers.clear();
        return passed;
    }
    
    protected boolean launchServer(Class<?> clz) {
        boolean ok = false;
        try { 
            ServerLauncher sl = new ServerLauncher(clz.getName());
            ok = sl.launchServer();
            assertTrue("server failed to launch", ok);
            launchers.add(sl);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("failed to launch server " + clz);
        }
        
        return ok;
    }
}
