/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.jms;

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
