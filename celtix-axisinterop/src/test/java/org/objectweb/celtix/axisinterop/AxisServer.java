package org.objectweb.celtix.axisinterop;

import java.io.File;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.logging.Logger;

import org.apache.axis.client.AdminClient;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.axis.utils.Options;
import org.objectweb.celtix.testutil.common.AbstractTestServerBase;

public class AxisServer extends AbstractTestServerBase {
    
    private static final int AXIS_PORT = 9360;
    private SimpleAxisServer axisServer;
    private boolean configExists;

    protected void run() {
        axisServer = new SimpleAxisServer();
        ServerSocket socket = null;
        final int retries = 5;
        for (int i = 0; i < retries; i++) {
            try {
                socket = new ServerSocket(AXIS_PORT);
                break;
            } catch (java.net.BindException be) {
                // Retry at 3 second intervals.
                if (i < (retries - 1)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                } else {
                    System.err.println("Failed to start axisServer on port : "
                        + Integer.toString(AXIS_PORT));
                    startFailed();
                }
            } catch (java.io.IOException ie) {
                System.err.println("Failed to start axisServer.");
                startFailed();
            }
        }
        axisServer.setServerSocket(socket);
        try {
            axisServer.start(true);
            AdminClient admin = new AdminClient();
            Options opts = new Options(new String[] {"-p", Integer.toString(AXIS_PORT)});
            InputStream is = getClass().getResourceAsStream("resources/echoDeploy.wsdd");
            String result = admin.process(opts, is);
            if (null == result || result.contains("AxisFault")) {
                throw new Exception("Failed to start axis server");
            }
        } catch (Exception ex) {
            System.err.println("Failed to deploy echo axis server.");
            axisServer.stop();
            axisServer = null;
            startFailed();
        }
    }

    public void start() {
        try {
            System.out.println("running server");
            run();
            System.out.println("signal ready");
            ready();

            // wait for a key press then shut
            // down the server
            //
            System.in.read();
            System.out.println("stopping bus");

        } catch (Throwable ex) {
            ex.printStackTrace();
            startFailed();
        } finally {
            if (verify(getLog())) {
                System.out.println("server passed");
            }
            System.out.println("server stopped");
        }
    }
    
    public void setUp() throws Exception {
        configExists = new File("server-config.wsdd").exists();
        if (configExists) {
            System.out.println("Warning: Found an axis server-config.wsdd file in working directory.");
        }
    }

    public void tearDown() throws Exception {
        if (null != axisServer) {
            axisServer.stop();
            axisServer = null;
        }
        // If there was no server-config.wsdd file before running the test
        // and we created one, then delete it.
        File serverConfig = new File("server-config.wsdd");
        if (!configExists && serverConfig.exists()) {
            System.out.println("Removing generated server-config.wsdd.");
            serverConfig.delete();
        }
    }

    protected boolean verify(Logger l) {
        AdminClient admin = new AdminClient();
        try {
            Options opts = new Options(new String[] {"-p", Integer.toString(AXIS_PORT)});
            InputStream is = getClass().getResourceAsStream("resources/echoUndeploy.wsdd");
            String result = admin.process(opts, is);
            if (null == result || result.contains("AxisFault")) {
                return false;
            }
            admin.quit(opts);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
 
    public static void main(String[] args) {
        try { 
            AxisServer s = new AxisServer();
            s.setUp(); 
            s.start();
            s.tearDown(); 
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
