package org.objectweb.celtix.systest.jms;

import org.activemq.broker.BrokerContainer;
import org.activemq.broker.impl.BrokerContainerImpl;
import org.activemq.store.vm.VMPersistenceAdapter;

import org.objectweb.celtix.systest.common.TestServerBase;

public class EmbeddedJMSBrokerLauncher extends TestServerBase {
    
    BrokerContainer container;
    final String brokerUrl1 = "tcp://localhost:61500";            
            
    public void tearDown() throws Exception {
        if (container != null) {
            container.stop();
        }
    }
            
    public void run() {
        try {                
            container = new BrokerContainerImpl();
            container.addConnector(brokerUrl1);
            container.setPersistenceAdapter(new VMPersistenceAdapter());
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
