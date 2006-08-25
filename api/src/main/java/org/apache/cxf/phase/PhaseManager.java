package org.apache.cxf.phase;

import java.util.List;

public interface PhaseManager {
    List<Phase> getInPhases();

    List<Phase> getOutPhases();
}
