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

package org.apache.cxf.systest.ws.rm;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.Control;
import org.apache.cxf.greeter_control.ControlService;
import org.apache.cxf.greeter_control.Greeter;
import org.apache.cxf.greeter_control.GreeterService;
import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.cxf.ws.rm.RMConstants;


/**
 * Tests the addition of WS-RM properties to application messages and the 
 * exchange of WS-RM protocol messages.
 */
public class SequenceTest extends ClientServerTestBase {

    private static final Logger LOG = Logger.getLogger(SequenceTest.class.getName());
    private static final String APP_NAMESPACE = "http://celtix.objectweb.org/greeter_control";
    private static final String GREETMEONEWAY_ACTION = APP_NAMESPACE + "/types/Greeter/greetMeOneWay";
    //private static final String GREETME_ACTION = APP_NAMESPACE + "/types/Greeter/greetMe";
    //private static final String GREETME_RESPONSE_ACTION = GREETME_ACTION + "Response";

    private Control control;
    private Bus greeterBus;
    private Greeter greeter;
    private String currentCfgResource;
    private MessageFlow mf;

    private boolean doTestOnewayAnonymousAcks = true;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SequenceTest.class);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SequenceTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
            
            public void setUp() throws Exception {
                startServers();
                LOG.fine("Started server");

                SpringBusFactory bf = new SpringBusFactory();
                Bus bus = bf.createBus();
                bf.setDefaultBus(bus);
                setBus(bus);
                LOG.fine("Created client bus");
            }
        };
    }

    public void setUp() throws Exception {
        super.setUp();
        ControlService service = new ControlService(); 
        LOG.fine("Created ControlService");
        control = service.getControlPort();
        LOG.fine("Created Control");
    }
    
    public void tearDown() {
        if (null != greeter) {
            assertTrue("Failed to stop greeter", control.stopGreeter());
            greeterBus.shutdown(true);
        }
    }

    // --- tests ---
    
    public void testOnewayAnonymousAcks() throws Exception {
        if (!doTestOnewayAnonymousAcks) {
            return;
        }
        setupGreeter("org/apache/cxf/systest/ws/rm/anonymous.xml");

        System.out.println("*** calling once");
        greeter.greetMeOneWay("once");
        System.out.println("*** calling twice");
        greeter.greetMeOneWay("twice");
        System.out.println("*** calling thrice");
        greeter.greetMeOneWay("thrice");

        // three application messages plus createSequence

        mf.verifyMessages(4, true);
        String[] expectedActions = new String[] {RMConstants.getCreateSequenceAction(), GREETMEONEWAY_ACTION,
                                                 GREETMEONEWAY_ACTION, GREETMEONEWAY_ACTION};
        expectedActions = new String[] {RMConstants.getCreateSequenceAction(), "", "", ""};
        mf.verifyActions(expectedActions, true);
        mf.verifyMessageNumbers(new String[] {null, "1", "2", "3"}, true);

        // createSequenceResponse plus 3 partial responses
        
        mf.verifyMessages(4, false);
        expectedActions = new String[] {RMConstants.getCreateSequenceResponseAction(), null, null, null};
        mf.verifyActions(expectedActions, false);
        mf.verifyMessageNumbers(new String[] {null, null, null, null}, false);
        mf.verifyAcknowledgements(new boolean[] {false, false, false, false}, false);
    }

    // --- test utilities ---

    private void setupGreeter(String cfgResource) {
        
        SpringBusFactory bf = new SpringBusFactory();
        greeterBus = bf.createBus(cfgResource);
        bf.setDefaultBus(greeterBus);

        OutMessageRecorder outRecorder = new OutMessageRecorder();
        greeterBus.getOutInterceptors().add(new JaxwsInterceptorRemover());
        greeterBus.getOutInterceptors().add(outRecorder);
        InMessageRecorder inRecorder = new InMessageRecorder();
        greeterBus.getInInterceptors().add(inRecorder);
        currentCfgResource = cfgResource;

        assertTrue("Failed to start greeter", control.startGreeter(cfgResource));
        
        GreeterService service = new GreeterService();
        greeter = service.getGreeterPort();
 
        mf = new MessageFlow(outRecorder.getOutboundMessages(), inRecorder.getInboundMessages());
        
        
    }
}
