/**
 * 
 */
package org.objectweb.celtix.transports.jms;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

import org.activemq.broker.impl.BrokerContainerImpl;
import org.activemq.store.vm.VMPersistenceAdapter;


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
        
        jmsBrokerThread = new JMSEmbeddedBroker(jmsBrokerUrl);
        jmsBrokerThread.startBroker();
    }
    
    public void tearDown() throws Exception {
        synchronized (this) {
            jmsBrokerThread.shutdownBroker = true;
        }
        if (jmsBrokerThread != null) {
            jmsBrokerThread.join();
        }
        
        jmsBrokerThread = null;
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
                    container.setPersistenceAdapter(new VMPersistenceAdapter());
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
