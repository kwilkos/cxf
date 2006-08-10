package org.objectweb.celtix.phase;

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
        // Collections.sort(inPhases);
    }
    
    final void createOutPhases() {
        outPhases = new ArrayList<Phase>();
        
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
    }
}
