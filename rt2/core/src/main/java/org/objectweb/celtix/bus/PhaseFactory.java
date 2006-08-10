package org.objectweb.celtix.bus;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.phase.Phase;

public class PhaseFactory {
  
    PhaseFactory(Bus b) {
    }
    
    List<Phase> createInPhases() {
        List<Phase> inPhases = new ArrayList<Phase>(); 
        
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
        // Collections.sort(inPhases);
        
        
        return inPhases;
    }
    
    List<Phase> createOutPhases() {
        List<Phase> outPhases = new ArrayList<Phase>();
        
        // TODO: get from configuration instead
        
        outPhases = new ArrayList<Phase>();
        int i = 0;
        
        outPhases.add(new Phase(Phase.POST_INVOKE, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.MARSHAL, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.CREATE_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.WRITE, ++i * 1000));
        outPhases.add(new Phase(Phase.SEND, ++i * 1000));
        
        // Collections.sort(outPhases);
        
        return outPhases;
    }
}
