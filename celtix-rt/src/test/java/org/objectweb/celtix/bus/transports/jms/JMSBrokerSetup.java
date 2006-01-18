/**
 * 
 */
package org.objectweb.celtix.bus.transports.jms;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

import org.activemq.broker.impl.BrokerContainerImpl;


class JMSBrokerSetup extends TestSetup {
    JMSEmbeddedBroker jmsBrokerThread;
    String jmsBrokerUrl = "tcp://localhost:61616";
    String activeMQStorageDir;
    public JMSBrokerSetup(TestSuite suite, String url) {
        super(suite);
        jmsBrokerUrl = url;
    }
    
    public JMSBrokerSetup(TestSuite suite) {
        super(suite);
    }
    
    public void setUp() throws Exception {
        
        // ActiveMQ will always use new subdirectory for each test run based on the
        // timestamp. This is required for proper cleanup. current directory structure 
        // doesn't get deleted due to lock held by jvm is not released even if the broker 
        // is down. 
//        
//        activeMQStorageDir = System.getProperty("activemq.store.dir");
//        File f1 =  new File(activeMQStorageDir);
//        deleteDir(f1);
        if (activeMQStorageDir != null) {
            System.setProperty("activemq.store.dir", 
                                 activeMQStorageDir + "/" + System.currentTimeMillis());
        } else {
            activeMQStorageDir = "./ActiveMQ";
        }
        
        jmsBrokerThread = new JMSEmbeddedBroker(jmsBrokerUrl);
        jmsBrokerThread.startBroker();
    }
    
    public void tearDown() throws Exception {
        synchronized (this) {
            jmsBrokerThread.shutdownBroker = true;
            notifyAll();
        }
        if (jmsBrokerThread != null) {
            jmsBrokerThread.join(10000L);
        }
        
        jmsBrokerThread = null;
        
        // Clean atleast the ActiveMQ repository from previous run. current run's 
        // repository is still locked in currnt JVM so cannot be cleaned. This will 
        // avoid clutter of the repositories. 
        
        if (!"./ActiveMQ".equals(activeMQStorageDir) 
            || null != System.getProperty("activemq.store.dir")) {
            System.setProperty("activemq.store.dir", activeMQStorageDir);
        }
        
        File f1 =  new File(activeMQStorageDir);
        deleteDir(f1);
    }
    
    private  boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length;  i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

    
    class JMSEmbeddedBroker extends Thread {
        boolean shutdownBroker;
        final String brokerUrl;
        Exception exception;
        
        
        public JMSEmbeddedBroker(String url) {
            brokerUrl = url;
        }
        
        public void startBroker() throws Exception {
            synchronized (this) {
                super.start();
                try {
                    wait();
                    if (exception != null) {
                        throw exception;
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        public void run() {
            try {  
                ContainerWapper container;
                synchronized (this) {
                    container = new ContainerWapper();
                    container.addConnector(brokerUrl);
                    container.start();
                    Thread.sleep(200);
                    notifyAll();
                }
                synchronized (this) {
                    while (!shutdownBroker) {
                        wait(1000);
                    }
                }
                container.shutdown();
                container = null;
            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
            }
        }
        
       
    }
    
    class ContainerWapper extends  BrokerContainerImpl {
        
        public void shutdown() {
            super.containerShutdown();
        }
    }
}
