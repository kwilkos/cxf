package org.objectweb.celtix.systest.jms;

import org.activemq.broker.BrokerContainer;
import org.activemq.broker.impl.BrokerContainerImpl;

import org.objectweb.celtix.systest.common.TestServerBase;

public class EmbeddedJMSBrokerLauncher extends TestServerBase {
    
    BrokerContainer container;
    final String brokerUrl = "tcp://localhost:61616";            
            
    public void tearDown() throws Exception {
        if (container != null) {
            container.stop();
        }
    }
            
    public void run() {
        try {                
            container = new BrokerContainerImpl();
            container.addConnector(brokerUrl);                    
            container.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            EmbeddedJMSBrokerLauncher s = new EmbeddedJMSBrokerLauncher();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
