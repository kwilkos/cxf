/**
 * 
 */
package org.objectweb.celtix.bus.transports.jms;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

import org.activemq.broker.BrokerContainer;
import org.activemq.broker.impl.BrokerContainerImpl;

class JMSBrokerSetup extends TestSetup {
    JMSEmbeddedBroker jmsBrokerThread;
    public JMSBrokerSetup(TestSuite suite) {
        super(suite);
    }
    
    public void setUp() throws Exception {
        jmsBrokerThread = new JMSEmbeddedBroker("tcp://localhost:61616");
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
                BrokerContainer container;
                synchronized (this) {
                    container = new BrokerContainerImpl();
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
                container.stop();
            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
            }
        }
    }
}
