package org.apache.cxf.phase;

import java.util.Comparator;

class PhaseComparator implements Comparator<Phase> {

    public int compare(Phase o1, Phase o2) {
        return o1.getPriority() - o2.getPriority();
    }
    
}
