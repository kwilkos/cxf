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

package org.apache.cxf.phase;

import java.util.ArrayList;
import java.util.List;

public class PhaseManagerImpl implements PhaseManager {
 
    private  List<Phase> inPhases;
    private  List<Phase> outPhases;
    
    public PhaseManagerImpl() {
        createInPhases();
        createOutPhases();
    } 

    public List<Phase> getInPhases() {
        return inPhases;
    }
  
    public List<Phase> getOutPhases() {
        return outPhases;
    }

    final void createInPhases() {
        inPhases = new ArrayList<Phase>(); 
        
        // TODO: get from configuration instead
        // bus.getConfiguration();
        
        int i = 0;
        
        inPhases = new ArrayList<Phase>();
        inPhases.add(new Phase(Phase.RECEIVE, ++i * 1000));        
        inPhases.add(new Phase(Phase.PRE_STREAM, ++i * 1000));
        inPhases.add(new Phase(Phase.USER_STREAM, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_STREAM, ++i * 1000));
        inPhases.add(new Phase(Phase.READ, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.USER_PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.UNMARSHAL, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_LOGICAL, ++i * 1000));
        inPhases.add(new Phase(Phase.USER_LOGICAL, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_LOGICAL, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_INVOKE, ++i * 1000));
        inPhases.add(new Phase(Phase.INVOKE, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_INVOKE, ++i * 1000));
        // Collections.sort(inPhases);
    }
    
    final void createOutPhases() {
        outPhases = new ArrayList<Phase>();
        
        // TODO: get from configuration instead
        
        outPhases = new ArrayList<Phase>();
        int i = 0;
        
        outPhases.add(new Phase(Phase.PRE_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.PREPARE_SEND, ++i * 1000));

        outPhases.add(new Phase(Phase.PRE_STREAM, ++i * 1000));
        
        outPhases.add(new Phase(Phase.PRE_PROTOCOL, ++i * 1000));        
        outPhases.add(new Phase(Phase.USER_PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_PROTOCOL, ++i * 1000));
        
        outPhases.add(new Phase(Phase.WRITE, ++i * 1000));
        outPhases.add(new Phase(Phase.MARSHAL, ++i * 1000));
        
        outPhases.add(new Phase(Phase.USER_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_STREAM, ++i * 1000));
        
        outPhases.add(new Phase(Phase.SEND, ++i * 1000));
        
        // Collections.sort(outPhases);
    }

}
