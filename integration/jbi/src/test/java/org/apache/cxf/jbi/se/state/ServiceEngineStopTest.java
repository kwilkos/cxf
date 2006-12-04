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

package org.apache.cxf.jbi.se.state;

import javax.jbi.JBIException;

import junit.framework.TestCase;

import org.apache.cxf.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStopTest extends TestCase {

    private ServiceEngineStateFactory stateFactory;
    private ServiceEngineStateMachine stop;
    
    public void setUp() throws Exception {
        stateFactory = ServiceEngineStateFactory.getInstance();
        stop = stateFactory.getStopState();
    }
    
    public void testStartOperation() throws Exception {
        stop.changeState(SEOperation.start, null);
        assertTrue(stateFactory.getCurrentState() instanceof ServiceEngineStart);
    }
    
    public void testShutdownOperation() throws Exception {
        stop.changeState(SEOperation.shutdown, null);
        assertTrue(stateFactory.getCurrentState() instanceof ServiceEngineShutdown);
    }
    
    public void testStopOperation() throws Exception {
        try {
            stop.changeState(SEOperation.stop, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
    
    public void testInitOperation() throws Exception {
        try {
            stop.changeState(SEOperation.init, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
}
