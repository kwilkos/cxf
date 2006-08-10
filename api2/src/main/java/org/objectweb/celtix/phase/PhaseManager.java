package org.objectweb.celtix.phase;

import java.util.List;

public interface PhaseManager {
    List<Phase> getInPhases();

    List<Phase> getOutPhases();

    List<Phase> getFaultPhases();
}
