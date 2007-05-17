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

package org.apache.cxf.ws.rm;

import java.math.BigInteger;
import java.util.Collection;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.ws.rm.persistence.RMMessage;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class RMManagerConfigurationTest extends Assert {

    @Test
    public void testConfiguration() {
        SpringBusFactory factory = new SpringBusFactory();
        Bus bus = factory.createBus("org/apache/cxf/ws/rm/custom-rmmanager.xml");
        RMManager manager = bus.getExtension(RMManager.class);
        assertNotNull(manager);
        assertTrue(manager.getSourcePolicy().getSequenceTerminationPolicy().isTerminateOnShutdown());
        assertEquals(10000L, manager.getRMAssertion().getBaseRetransmissionInterval()
                     .getMilliseconds().longValue());
        assertEquals(10000L, manager.getRMAssertion().getAcknowledgementInterval()
                     .getMilliseconds().longValue());        
        TestStore store = (TestStore)manager.getStore();
        assertEquals("here", store.getLocation());
        
    }
    
    static class TestStore implements RMStore {
        
        private String location;
        
        public TestStore() {
            // this(null);
        }
        
        /*
        public TestStore(String l) {
            location = l;
        }
        */
        
        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }



        public void createDestinationSequence(DestinationSequence seq) {
            // TODO Auto-generated method stub
            
        }

        public void createSourceSequence(SourceSequence seq) {
            // TODO Auto-generated method stub
            
        }

        public Collection<DestinationSequence> getDestinationSequences(String endpointIdentifier) {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection<RMMessage> getMessages(Identifier sid, boolean outbound) {
            // TODO Auto-generated method stub
            return null;
        }

        public Collection<SourceSequence> getSourceSequences(String endpointIdentifier) {
            // TODO Auto-generated method stub
            return null;
        }

        public void persistIncoming(DestinationSequence seq, RMMessage msg) {
            // TODO Auto-generated method stub
            
        }

        public void persistOutgoing(SourceSequence seq, RMMessage msg) {
            // TODO Auto-generated method stub
            
        }

        public void removeDestinationSequence(Identifier seq) {
            // TODO Auto-generated method stub
            
        }

        public void removeMessages(Identifier sid, Collection<BigInteger> messageNrs, boolean outbound) {
            // TODO Auto-generated method stub
            
        }

        public void removeSourceSequence(Identifier seq) {
            // TODO Auto-generated method stub
            
        }
        
    }
}
