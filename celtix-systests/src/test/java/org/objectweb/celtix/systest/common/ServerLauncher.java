package org.objectweb.celtix.systest.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ServerLauncher {

    public static final int DEFAULT_TIMEOUT = 3 * 60 * 1000;
    private static final Logger LOG = Logger.getLogger(ServerLauncher.class.getName());

    boolean serverPassed;
    final String className;


    private final boolean debug = false;
    private final boolean inProcess = false;
    private TestServerBase inProcessServer;
    
    private final String javaExe;
    private Process process;
    private boolean serverIsReady;
    private boolean serverIsStopped;
    private boolean serverLaunchFailed;

    private final Mutex mutex = new Mutex();

    public ServerLauncher(String theClassName) {
        className = theClassName;
        javaExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }

    private boolean waitForServerToStop() {
        synchronized (mutex) {
            while (!serverIsStopped) {
                try {
                    TimeoutCounter tc = new TimeoutCounter(DEFAULT_TIMEOUT);
                    mutex.wait(DEFAULT_TIMEOUT);
                    if (tc.isTimeoutExpired()) {
                        System.out.println("destroying server process");
                        process.destroy();
                        break;
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if (!inProcess) {
                //wait for process to end...
                TimeoutCounter tc = new TimeoutCounter(DEFAULT_TIMEOUT);
                while (!tc.isTimeoutExpired()) {
                    try {
                        process.exitValue();
                        break;
                    } catch (IllegalThreadStateException ex) {
                        //ignore, process hasn't ended
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex1) {
                            //ignore
                        }
                    }
                }
                if (tc.isTimeoutExpired()) {
                    process.destroy();
                }
            }
        }
        return serverIsStopped;
    }

    public boolean stopServer() throws IOException {
        if (inProcess) {
            try {
                return inProcessServer.stopInProcess();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IOException(ex.getMessage());
            }
        } else {
            if (process != null) {
                process.getOutputStream().write('q');
                process.getOutputStream().write('\n');
                process.getOutputStream().flush();
                waitForServerToStop();
                process.destroy();
            }
        }
        return serverPassed;
    }

    public boolean launchServer() throws IOException {

        serverIsReady = false;
        serverLaunchFailed = false;

        if (inProcess) {
            Class<?> cls;
            try {
                cls = Class.forName(className);
                Class<? extends TestServerBase> svcls = cls.asSubclass(TestServerBase.class);
                inProcessServer = svcls.newInstance();
                inProcessServer.startInProcess();
                serverIsReady = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                serverLaunchFailed = true;
            }
        } else {
            List<String> cmd = getCommand();

            if (debug) {
                System.err.print("CMD: ");
            }
            
            
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            process = pb.start();
    
            launchOutputMonitorThread(process.getInputStream(), System.out);
    
            synchronized (mutex) {
                do {
                    TimeoutCounter tc = new TimeoutCounter(DEFAULT_TIMEOUT);
                    try {
                        mutex.wait(DEFAULT_TIMEOUT);
                        if (tc.isTimeoutExpired()) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (!serverIsReady && !serverLaunchFailed);
            }
        }
        return serverIsReady;
    }

    public int waitForServer() {
        int ret = -1;
        try {
            process.waitFor();
            ret = process.exitValue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void launchOutputMonitorThread(final InputStream in, final PrintStream out) {
        Thread t = new OutputMonitorThread(in, out);
        t.start();
    }
    private class OutputMonitorThread extends Thread {
        InputStream in;
        PrintStream out;

        OutputMonitorThread(InputStream i, PrintStream o) {
            in = i;
            out = o;
        }

        public void run() {
            try {
                StringBuilder serverOutput = new StringBuilder();
                String outputDir = System.getProperty("server.output.dir", "target/surefire-reports");
                FileOutputStream fos = new FileOutputStream(outputDir + className + ".out");
                PrintStream ps = new PrintStream(fos);
                boolean running = true;
                for (int ch = in.read(); ch != -1; ch = in.read()) {
                    serverOutput.append((char)ch);
                    if (debug) {
                        System.err.print((char)ch);
                    }
                    String s = serverOutput.toString();
                    if (s.contains("server ready")) {
                        notifyServerIsReady();
                    } else if (s.contains("server passed")) {
                        serverPassed = true;
                    } else if (s.contains("server stopped")) {
                        notifyServerIsStopped();
                        running = false;
                    } else if (s.contains("failed")) {
                        notifyServerLaunchFailed();
                        running = false;
                    }
                    if (ch == '\n' || !running) {
                        synchronized (out) {
                            ps.print(serverOutput.toString());
                            serverOutput = new StringBuilder();
                            ps.flush();
                        }
                    }
                }
                
            } catch (IOException ex) {
                if (!ex.getMessage().contains("Stream closed")) {
                    ex.printStackTrace();
                }
            }
        }
    }

    void notifyServerIsReady() {
        synchronized (mutex) {
            serverIsReady = true;
            mutex.notifyAll();
        }
    }

    void notifyServerIsStopped() {
        synchronized (mutex) {
            LOG.info("notify server stopped");
            serverIsStopped = true;
            mutex.notifyAll();
        }
    }

    void notifyServerLaunchFailed() {
        synchronized (mutex) {
            serverLaunchFailed = true;
            mutex.notifyAll();
        }
    }

    private List<String> getCommand() {

        List<String> cmd = new ArrayList<String>();
        cmd.add(javaExe);
        cmd.add("-classpath");
        
        ClassLoader loader = this.getClass().getClassLoader();
        StringBuffer classpath = new StringBuffer(System.getProperty("java.class.path"));
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlloader = (URLClassLoader)loader; 
            for (URL url : urlloader.getURLs()) {
                classpath.append(File.pathSeparatorChar);
                classpath.append(url.getFile());
            }
        }
        cmd.add(classpath.toString());
        
        cmd.add("-Djavax.xml.ws.spi.Provider=org.objectweb.celtix.bus.jaxws.spi.ProviderImpl");
        /* REVISIT: this prevents the server from shutting down and causes the test to timeout.
         * It would be good however to get this fixed as it is useful for server debugging. 
         */
        /*
        String loggingPropertiesFile = System.getProperty("java.util.logging.config.file");
        if (null != loggingPropertiesFile) {
            cmd.add("-Djava.util.logging.config.file=" + loggingPropertiesFile);
        } 
        */ 
     
        cmd.add(className);

        return cmd;
    }

    static class Mutex {
        // empty
    }

    static class TimeoutCounter {
        private final long expectedEndTime;

        public TimeoutCounter(long theExpectedTimeout) {
            expectedEndTime = System.currentTimeMillis() + theExpectedTimeout;
        }

        public boolean isTimeoutExpired() {
            return System.currentTimeMillis() > expectedEndTime;
        }
    }
}
