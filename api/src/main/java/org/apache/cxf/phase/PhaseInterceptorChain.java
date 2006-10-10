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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Message;

/**
 * A PhaseInterceptorChain orders Interceptors according to the phase the
 * particpate in and also according to the before & after properties on an
 * Interceptor.
 * <p>
 * A List of phases is supplied to the PhaseInterceptorChain in the constructor.
 * Interceptors that are added to the chain are ordered by phase. Within that
 * phases interceptors can order themselves. Each PhaseInterceptor has an ID.
 * PhaseInterceptors can supply a Collection of IDs which they should run before
 * or after, supplying fine grained ordering.
 * <p>
 *  
 * @author Dan Diephouse
 */
public class PhaseInterceptorChain implements InterceptorChain {

    private static final Logger LOG = Logger.getLogger(PhaseInterceptorChain.class.getName());
    
    private final Map<Phase, List<Interceptor>> interceptors = new TreeMap<Phase, List<Interceptor>>();
    private final Map<String, List<Interceptor>> nameMap = new HashMap<String, List<Interceptor>>();

    private State state;
    private PhaseInterceptorIterator iterator;
    private Message pausedMessage;
    private Interceptor faultInterceptor;
    
    public PhaseInterceptorChain(List<Phase> ps) {
        state = State.EXECUTING;

        for (Phase phase : ps) {
            List<Interceptor> ints = new ArrayList<Interceptor>();
            interceptors.put(phase, ints);
            nameMap.put(phase.getName(), ints);
        }
        iterator = new PhaseInterceptorIterator();
    }

    public void add(List<Interceptor> newhandlers) {
        if (newhandlers == null) {
            return;
        }

        for (Interceptor handler : newhandlers) {
            add(handler);
        }
    }

    @SuppressWarnings("unchecked")
    public void add(Interceptor i) {
        PhaseInterceptor pi = (PhaseInterceptor)i;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Adding interceptor " + i + " to phase " + pi.getPhase());
        }

        String phaseName = pi.getPhase();
        
        List<Interceptor> phase = nameMap.get(phaseName);
        if (phase == null) {
            LOG.fine("Phase " + phaseName + " does not exist. Skipping handler "
                      + i.getClass().getName());
        } else {
            insertInterceptor(phase, pi);
        }
    }

    public void pause() {
        state = State.PAUSED;
    }

    public void resume() {
        if (state == State.PAUSED) {
            state = State.EXECUTING;
            doIntercept(pausedMessage);
        }
    }
    

    /**
     * Invokes each phase's handler in turn.
     * 
     * @param context
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public boolean doIntercept(Message message) {
        while (state == State.EXECUTING && iterator.hasNext()) {
            try {
                Interceptor currentInterceptor = iterator.next();
               
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invoking handleMessage on interceptor " + currentInterceptor);
                }
                currentInterceptor.handleMessage(message);
            } catch (Exception ex) {
                if (LOG.isLoggable(Level.INFO)) {
                    LogUtils.log(LOG, Level.INFO, "Interceptor has thrown exception, unwinding now", ex);
                }
                message.setContent(Exception.class, ex);
                unwind(message);
                
                if (faultInterceptor != null) {
                    faultInterceptor.handleMessage(message);
                }
                state = State.ABORTED;
            } 
        }
        if (state == State.EXECUTING) {
            state = State.COMPLETE;
        } else if (state == State.PAUSED) {
            pausedMessage = message;
        }
        return state == State.COMPLETE;
    }
    
    public void reset() {
        if (state == State.COMPLETE) {
            state = State.EXECUTING;
            iterator.reset();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void unwind(Message message) {
        while (iterator.hasPrevious()) {
            Interceptor currentInterceptor = iterator.previous();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Invoking handleFault on interceptor " + currentInterceptor);
            }
            currentInterceptor.handleFault(message);
        }
    }


    public void remove(Interceptor i) {
        // TODO
    }

    public Iterator<Interceptor<? extends Message>> iterator() {
        return getIterator();
    }
    public ListIterator<Interceptor<? extends Message>> getIterator() {
        return new PhaseInterceptorIterator();
    }
    

    protected void insertInterceptor(List<Interceptor> intercs, PhaseInterceptor interc) {
        if (intercs.size() == 0) {
            intercs.add(interc);
            return;
        }

        int begin = -1;
        int end = intercs.size();

        Collection before = interc.getBefore();
        Collection after = interc.getAfter();

        for (int i = 0; i < intercs.size(); i++) {
            PhaseInterceptor cmp = (PhaseInterceptor)intercs.get(i);

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

    
    class PhaseInterceptorIterator implements ListIterator<Interceptor<? extends Message>> {
        List<Interceptor<? extends Message>> called
            = new ArrayList<Interceptor<? extends Message>>();
        
        Iterator<List<Interceptor>> phases;
        List<Interceptor> currentPhase;
        Iterator<Interceptor> currentPhaseIterator;
        Interceptor<? extends Message> last;
        
        PhaseInterceptorIterator() {
            phases = interceptors.values().iterator();
            if (phases.hasNext()) {
                currentPhase = phases.next();
                currentPhaseIterator = currentPhase.iterator();
                last = null;
            }
        }
        public boolean hasNext() {
            if (currentPhaseIterator != null) {
                try {
                    if (currentPhaseIterator.hasNext()) {
                        return true; 
                    }
                    nextPhase();
                } catch (ConcurrentModificationException cme) {
                    refreshIterator();
                }
                return hasNext();
            }
            return false;
        }
        private void refreshIterator() {
            currentPhaseIterator = currentPhase.iterator();
            if (last != null) {
                while (currentPhaseIterator.hasNext()
                    && last != currentPhaseIterator.next()) {
                    //nothing
                }
            }
        }
        private void nextPhase() {
            if (phases.hasNext()) {
                currentPhase = phases.next();
                currentPhaseIterator = currentPhase.iterator();
                last = null;
            } else {
                currentPhase = null;
                currentPhaseIterator = null;
                last = null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public Interceptor<? extends Message> next() {
            if (currentPhaseIterator != null) {
                try {
                    last = currentPhaseIterator.next();
                    called.add(last);
                    return last;
                } catch (ConcurrentModificationException cme) {
                    refreshIterator();
                    return next();
                }                
            }
            return null;
        }

        public boolean hasPrevious() {
            return !called.isEmpty();
        }
        public Interceptor<? extends Message> previous() {
            return called.remove(called.size() - 1);
        }

        public int nextIndex() {
            throw new UnsupportedOperationException();
        }
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }
        public void add(Interceptor o) {
            throw new UnsupportedOperationException();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        public void set(Interceptor o) {
            throw new UnsupportedOperationException();
        }

        protected void reset() {
            phases = interceptors.values().iterator();
            if (phases.hasNext()) {
                currentPhase = phases.next();
                currentPhaseIterator = currentPhase.iterator();
                last = null;
            }
        }
    }

    public Interceptor getFaultInterceptor() {
        return faultInterceptor;
    }

    public void setFaultInterceptor(Interceptor faultInterceptor) {
        this.faultInterceptor = faultInterceptor;
    }

}
