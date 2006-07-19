package org.objectweb.celtix.phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.message.Message;

/**
 * A PhaseInterceptorChain orders Interceptors according to the
 * phase the particpate in and also according to the before & after
 * properties on an Interceptor.
 * <p>
 * A List of phases is supplied to the PhaseInterceptorChain in the 
 * constructor. Interceptors that are added to the chain are ordered by
 * phase. Within that phases interceptors can order themselves. Each
 * PhaseInterceptor has an ID. PhaseInterceptors can supply a Collection
 * of IDs which they should run before or after, supplying fine grained
 * ordering.
 * <p>
 * NOTE: This is roughly modeled on the AXIS2 phase & handler ideas.
 * @author Dan Diephouse
 */
public class PhaseInterceptorChain implements InterceptorChain {

    private static final Logger LOG = Logger.getLogger(PhaseInterceptorChain.class.getName());
    private Map<String, List<Interceptor>> interceptors = new HashMap<String, List<Interceptor>>();
    private List<Phase> phases;
    private Phase currentPhase;
    private List<Interceptor> currentPhaseInterceptors;
    private Interceptor currentInterceptor;
    
    public PhaseInterceptorChain(List<Phase> ps) {
        // Order the phases correctly based on priority
        Collections.sort(ps, new PhaseComparator());
        this.phases = ps;
        
        for (Iterator itr = ps.iterator(); itr.hasNext();) {
            Phase phase = (Phase) itr.next();
            interceptors.put(phase.getName(), new ArrayList<Interceptor>());
        }
        
        currentPhase = ps.get(0);
        currentPhaseInterceptors = interceptors.get(currentPhase.getName());
    }

    public void add(List<Interceptor> newhandlers) {
        if (newhandlers == null) {
            return;
        }
        
        for (Iterator itr = newhandlers.iterator(); itr.hasNext();) {
            Interceptor handler = (Interceptor) itr.next();
            add(handler);
        }
    }

    public void add(Interceptor i) {
        AbstractPhaseInterceptor pi = (AbstractPhaseInterceptor) i;
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("adding handler " + i + " to phase " + pi.getPhase());
        }

        List<Interceptor> phase = interceptors.get(pi.getPhase());
        
        if (phase == null)  {
            LOG.fine("Phase " + pi.getPhase()
                     + " does not exist. Skipping handler "
                     + i.getClass().getName());
        } else {
            insertInterceptor(phase, pi);
        }
    }

    /**
     * Invokes each phase's handler in turn.
     * 
     * @param context
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void doIntercept(Message message) {
        if (currentInterceptor == null) {
            currentInterceptor = currentPhaseInterceptors.get(0);
        } else {
            int index = currentPhaseInterceptors.indexOf(currentInterceptor);
            if (index == currentPhaseInterceptors.size() - 1) {
                // we're at the end of this phase, go to the next one
                int phaseIndex = phases.indexOf(currentPhase);

                if (setupPhase(++phaseIndex)) {
                    return;
                }
            } else {
                // Find the current position of this interceptor as it could have changed
                currentInterceptor = currentPhaseInterceptors.get(index + 1);
            }
        }

        currentInterceptor.handleMessage(message);
    }

    private boolean setupPhase(int phaseIndex) {
        // Have we reached the end of the chain??
        if (phaseIndex == phases.size()) {
            return true;
        }
        
        currentPhase = phases.get(phaseIndex);
        currentPhaseInterceptors = interceptors.get(currentPhase.getName());
        if (currentPhaseInterceptors.size() == 0) {
            return setupPhase(++phaseIndex);
        }
        
        currentInterceptor = currentPhaseInterceptors.get(0);
        return false;
    }

    public void remove(Interceptor i) {
        // TODO
    }
    
    public Iterator<Interceptor> getIterator() {
        List<Interceptor> allInterceptors = new ArrayList<Interceptor>();
        for (List<Interceptor> interceptorList : interceptors.values()) {
            allInterceptors.addAll(interceptorList);
        }
        return allInterceptors.iterator();
    }
    
    protected void insertInterceptor(List<Interceptor> intercs, AbstractPhaseInterceptor interc) {
        if (intercs.size() == 0) {
            intercs.add(interc);
            return;
        }
        
        int begin = -1;
        int end = intercs.size();
        
        Collection before = interc.getBefore();
        Collection after = interc.getAfter();

        for (int i = 0; i < intercs.size(); i++) {
            PhaseInterceptor cmp = (PhaseInterceptor) intercs.get(i);

            if (cmp.getId() == null) {
                continue;
            }
            
            if (before.contains(cmp.getId()) && i < end) {
                end = i;
            }

            if (cmp.getBefore().contains(interc.getId()) && i > begin) {
                begin = i;
            }
            
            if (after.contains(cmp.getId()) && i > begin) {
                begin = i;
            }
            
            if (cmp.getAfter().contains(interc.getId()) && i < end) {
                end = i;
            }
        }

        if (end < begin + 1) {
            throw new IllegalStateException("Invalid ordering for handler " + interc.getClass().getName());
        }
        
        intercs.add(begin + 1, interc);
    }
}
