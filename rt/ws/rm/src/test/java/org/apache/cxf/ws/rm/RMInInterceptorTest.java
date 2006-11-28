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

import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.addressing.MAPAggregator;

public class RMInInterceptorTest extends TestCase {
    
    
    public void testOrdering() {
        Phase p = new Phase(Phase.PRE_LOGICAL, 1);
        PhaseInterceptorChain chain = 
            new PhaseInterceptorChain(Collections.singletonList(p));
        MAPAggregator map = new MAPAggregator();
        RMInInterceptor rmi = new RMInInterceptor();        
        chain.add(rmi);
        chain.add(map);
        Iterator it = chain.iterator();
        assertSame("Unexpected order.", rmi, it.next());
        assertSame("Unexpected order.", map, it.next());
                              
    } 
}
