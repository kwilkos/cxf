package org.objectweb.celtix.systest.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public abstract class ClientServerTestBase extends TestCase {

    static { 
        System.setProperty("javax.xml.ws.EndpointFactory", "org.objectweb.celtix.bus.EndpointFactoryImpl");
        System.setProperty("javax.xml.ws.ServiceFactory", "org.objectweb.celtix.bus.ServiceFactoryImpl");
    }

    private Executor executor = Executors.newCachedThreadPool();
    private List<ServerTask> serverTasks = new ArrayList<ServerTask>();
    
    private Bus bus; 

    public void setUp() throws BusException {        
        bus = Bus.init();
    }

    public void tearDown() throws BusException {
        stopServers();
        if (bus != null) { 
            bus.shutdown(true);
        }
    }

    protected void runServer(TestServerBase serverBase) {
        
        ServerTask st = new ServerTask(serverBase);
        assertTrue("server failed to start", st.submitAndWaitForServerStart());
        serverTasks.add(st);
    }
    
    protected void stopServers() {
        
        for (ServerTask st : serverTasks) { 
            st.stop();
        }
    }
    

    class ServerTask  implements Runnable {
        
        private TestServerBase server; 
                
        public ServerTask(TestServerBase theServer) {
            server = theServer; 
        }
                
        public void stop() { 
            server.stopServer();
        }
        
        
        public void run() { 
            try { 
                server.run();
            } catch (Exception ex) {
                StringWriter stackTrace = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTrace));
                fail(stackTrace.toString());
            }
        }
        
        boolean submitAndWaitForServerStart() {            
            executor.execute(this);
            return server.waitForReady();
        }
        
    }    
}
