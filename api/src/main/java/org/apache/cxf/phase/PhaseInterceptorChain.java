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
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.MessageObserver;

/**
 * A PhaseInterceptorChain orders Interceptors according to the phase they
 * participate in and also according to the before & after properties on an
 * Interceptor.
 * <p>
 * A List of phases is supplied to the PhaseInterceptorChain in the constructor.
 * Interceptors that are added to the chain are ordered by phase. Within a
 * phase, interceptors can order themselves. Each PhaseInterceptor has an ID.
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
    private Stack<State> subChainState;
    private PhaseInterceptorIterator iterator;
    private Message pausedMessage;
    private MessageObserver faultObserver;
    private Object pauseWaitObject = new Object();
    
    // currently one chain for one request/response, use below as signal to avoid duplicate fault processing
    // on nested calling of doIntercept(), which will throw same fault multi-times
    private boolean faultOccured;
    
    public PhaseInterceptorChain(List<Phase> ps) {
        state = State.EXECUTING;
        subChainState = new Stack<State>();

        for (Phase phase : ps) {
            List<Interceptor> ints = new ArrayList<Interceptor>();
            interceptors.put(phase, ints);
            nameMap.put(phase.getName(), ints);
        }
        iterator = new PhaseInterceptorIterator();
    }
    
    public void add(List<Interceptor> newhandlers) {
        add(newhandlers, false);
    }

    public void add(List<Interceptor> newhandlers, boolean force) {
        if (newhandlers == null) {
            return;
        }

        for (Interceptor handler : newhandlers) {
            add(handler, force);
        }
    }

    public void add(Interceptor i) {
        add(i, false);
    }
    
    public void add(Interceptor i, boolean force) {
        PhaseInterceptor pi = (PhaseInterceptor)i;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Adding interceptor " + i + " to phase " + pi.getPhase());
        }

        String phaseName = pi.getPhase();
        
        List<Interceptor> phase = nameMap.get(phaseName);
        if (phase == null) {
            LOG.fine("Phase " + phaseName + " does not exist. Skipping handler "
                      + i.getClass().getName());
        } else if (force 
            | !containsType(CastUtils.cast(phase, PhaseInterceptor.class), pi.getId())) {            
            insertInterceptor(phase, pi);
        }
    }

    public void pause() {
        state = State.PAUSED;
    }

    public void finishSubChain() {
        if (!subChainState.isEmpty()) {
            subChainState.pop();
            subChainState.push(State.SUBCHAIN_COMPLETE);
        }
    }

    public void resume() {
        if (state == State.PAUSED) {
            state = State.EXECUTING;
            
            if (pausedMessage == null) {
                synchronized (pauseWaitObject) {
                    try {
                        pauseWaitObject.wait(1000);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
            
            doIntercept(pausedMessage);
        }
    }
    

    /**
     * Intercept a message, invoking each phase's handlers in turn.
     * 
     * @param message the message 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public boolean doIntercept(Message message) {
        while (state == State.EXECUTING && iterator.hasNext()) {
            try {
                Interceptor currentInterceptor = iterator.next();
               
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Invoking handleMessage on interceptor " + currentInterceptor);
                }
                currentInterceptor.handleMessage(message);
                
                if (!subChainState.empty() && subChainState.peek() == State.SUBCHAIN_COMPLETE) {
                    return true;
                }
            } catch (RuntimeException ex) {
                if (!faultOccured) {
                    if (subChainState.size() > 0 
                        && subChainState.peek().equals(State.EXECUTING)) {
                        throw ex;
                    }
 
                    faultOccured = true;
                    if (LOG.isLoggable(Level.INFO)) {
                        LogUtils.log(LOG, Level.INFO, "Interceptor has thrown exception, unwinding now", ex);
                    }
                    message.setContent(Exception.class, ex);
                    if (message.getExchange() != null) {
                        message.getExchange().put(Exception.class, ex);
                    }                    
                    unwind(message);
                    
                    if (faultObserver != null) {
                        faultObserver.onMessage(message);
                    }
                }
                state = State.ABORTED;
            } 
        }
        if (state == State.EXECUTING) {
            state = State.COMPLETE;
        } else if (state == State.PAUSED) {
            pausedMessage = message;
            synchronized (pauseWaitObject) {
                pauseWaitObject.notifyAll();
            }
        }
        return state == State.COMPLETE;
    }
    
    /**
     * Intercept a message, invoking each phase's handlers in turn,
     * starting after the specified interceptor.
     * 
     * @param message the message
     * @param startingAfterInterceptorID the id of the interceptor 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public boolean doIntercept(Message message, String startingAfterInterceptorID) {
        while (state == State.EXECUTING && iterator.hasNext()) {
            PhaseInterceptor currentInterceptor = (PhaseInterceptor)iterator.next();
            if (currentInterceptor.getId().equals(startingAfterInterceptorID)) {
                break;
            }
        }
        return doIntercept(message);
    }
    
    /**
     * Invokes the following inteceptors in a sub chain until the last chain in the
     * sub chain calls finishSubChain, which makes the flow continue in the
     * main chain.
     * 
     * @param message the message
     * @throws Exception
     */
    public boolean doInterceptInSubChain(Message message) {
        subChainState.push(State.SUBCHAIN_EXECUTING);
        boolean result = doIntercept(message);
        subChainState.pop();
        return result;
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
        PhaseInterceptorIterator it = new PhaseInterceptorIterator();
        while (it.hasNext()) {
            if (it.next() == i) {
                it.remove();
            }
        }
    }
    

    public void abort() {
        this.state = InterceptorChain.State.ABORTED;
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
                if (end < begin) {
                    intercs.remove(cmp);
                    intercs.add(end, cmp);
                    i = end;
                    begin = end;
                    end = begin + 1;
                }
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

    void outputChainToLog(boolean modified) {
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder chain = new StringBuilder();
            
            if (modified) {
                chain.append("Chain ")
                    .append(toString())
                    .append(" was modified. Current flow:\n");
            } else {
                chain.append("Chain ")
                    .append(toString())
                    .append(" was created. Current flow:\n");
            }
            
            for (Map.Entry<Phase, List<Interceptor>> entry : interceptors.entrySet()) {
                chain.append("  ")
                    .append(entry.getKey().getName())
                    .append(" [");
                
                boolean first = true;
                for (Interceptor i : entry.getValue()) {
                    if (first) {
                        first = false;
                    } else {
                        chain.append(", ");
                    }
                    chain.append(i.getClass().getSimpleName());
                }
                
                chain.append("]\n");
            }
            LOG.fine(chain.toString());
        }
    }
    
    boolean containsType(List<PhaseInterceptor> phase, String id) {
        for (PhaseInterceptor pi : phase) {
            if (id.equals(pi.getId())) {
                return true;
            }
        }
        return false;
    }
    
    class PhaseInterceptorIterator implements ListIterator<Interceptor<? extends Message>> {
        List<Interceptor<? extends Message>> called
            = new ArrayList<Interceptor<? extends Message>>();
        
        Iterator<List<Interceptor>> phases;
        List<Interceptor> currentPhase;
        Iterator<Interceptor> currentPhaseIterator;
        Interceptor<? extends Message> last;
        boolean first = true;
        
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
            
            if (first) {
                outputChainToLog(false);
                first = false;
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
            if (currentPhaseIterator != null) {
                currentPhaseIterator.remove();
            }
            // throw new UnsupportedOperationException();
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
            outputChainToLog(true);
        }
    }



    public MessageObserver getFaultObserver() {
        return faultObserver;
    }
    

    public void setFaultObserver(MessageObserver faultObserver) {
        this.faultObserver = faultObserver;
    }
    
    

}
