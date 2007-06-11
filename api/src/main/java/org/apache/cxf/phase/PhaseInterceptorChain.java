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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
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
 */
public class PhaseInterceptorChain implements InterceptorChain {

    private static final Logger LOG = Logger.getLogger(PhaseInterceptorChain.class.getName());

    
    private final Map<String, PhaseHolder> nameMap = new HashMap<String, PhaseHolder>();
    private final PhaseHolder firstPhase;
    
    private State state;
    private Message pausedMessage;
    private MessageObserver faultObserver;
    private PhaseInterceptorIterator iterator;
    
    // currently one chain for one request/response, use below as signal to avoid duplicate fault processing
    // on nested calling of doIntercept(), which will throw same fault multi-times
    private boolean faultOccured;
    
    public PhaseInterceptorChain(SortedSet<Phase> ps) {
        state = State.EXECUTING;

        PhaseHolder last = null;
        PhaseHolder first = null;
        for (Phase phase : ps) {
            PhaseHolder ph = new PhaseHolder(phase);
            if (first == null) {
                first = ph;
            }
            if (last != null) {
                last.next = ph;
            }
            ph.prev = last;
            last = ph;
            nameMap.put(phase.getName(), ph);
        }
        firstPhase = first;
    }
    
    private void updateIterator() {
        if (iterator == null) {
            iterator = new PhaseInterceptorIterator(firstPhase);
            outputChainToLog(false);
            //System.out.println(toString());
        }
    }
    
    public void add(Collection<Interceptor> newhandlers) {
        add(newhandlers, false);
    }

    public void add(Collection<Interceptor> newhandlers, boolean force) {
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
        
        PhaseHolder phase = nameMap.get(phaseName);
        if (phase == null) {
            LOG.fine("Phase " + phaseName + " does not exist. Skipping handler "
                      + i.getClass().getName());
        } else {            
            insertInterceptor(phase, pi, force);
        }
    }

    public synchronized void pause() {
        state = State.PAUSED;
    }

    public synchronized void resume() {
        if (state == State.PAUSED) {
            state = State.EXECUTING;
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
    public synchronized boolean doIntercept(Message message) {
        updateIterator();
        
        pausedMessage = message;
        while (state == State.EXECUTING && iterator.hasNext()) {
            try {
                Interceptor currentInterceptor = iterator.next();
               
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Invoking handleMessage on interceptor " + currentInterceptor);
                }
                currentInterceptor.handleMessage(message);
                
            } catch (RuntimeException ex) {
                if (!faultOccured) {
 
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
    public synchronized boolean doInterceptStartingAfter(Message message,
                                                         String startingAfterInterceptorID) {
        updateIterator();
        while (state == State.EXECUTING && iterator.hasNext()) {
            PhaseInterceptor currentInterceptor = (PhaseInterceptor)iterator.next();
            if (currentInterceptor.getId().equals(startingAfterInterceptorID)) {
                break;
            }
        }
        return doIntercept(message);
    }

    /**
     * Intercept a message, invoking each phase's handlers in turn,
     * starting at the specified interceptor.
     * 
     * @param message the message
     * @param startingAtInterceptorID the id of the interceptor 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean doInterceptStartingAt(Message message,
                                                         String startingAtInterceptorID) {
        updateIterator();
        while (state == State.EXECUTING && iterator.hasNext()) {
            PhaseInterceptor currentInterceptor = (PhaseInterceptor)iterator.next();
            if (currentInterceptor.getId().equals(startingAtInterceptorID)) {
                iterator.previous();
                break;
            }
        }
        return doIntercept(message);
    }

    public synchronized void reset() {
        updateIterator();
        if (state == State.COMPLETE) {
            state = State.EXECUTING;
            iterator.reset();
        } else {
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
        PhaseInterceptorIterator it = new PhaseInterceptorIterator(firstPhase);
        while (it.hasNext()) {
            InterceptorHolder holder = it.nextInterceptorHolder();
            if (holder.interceptor == i) {
                remove(holder);
                return;
            }
        }
    }

    public synchronized void abort() {
        this.state = InterceptorChain.State.ABORTED;
    }

    public Iterator<Interceptor<? extends Message>> iterator() {
        return getIterator();
    }
    public ListIterator<Interceptor<? extends Message>> getIterator() {
        return new PhaseInterceptorIterator(firstPhase);
    }

    private void remove(InterceptorHolder i) {
        if (i.prev != null) {
            i.prev.next = i.next;
        }
        if (i.next != null) {
            i.next.prev = i.prev;
        }
        PhaseHolder ph = i.phase;
        if (ph.head == i) {
            if (i.next != null
                && i.next.phase == ph) {
                ph.head = i.next;
            } else {
                ph.head = null;
                ph.tail = null;
            }
        }
        if (ph.tail == i) {
            if (i.prev != null
                && i.prev.phase == ph) {
                ph.tail = i.prev;
            } else {
                ph.head = null;
                ph.tail = null;
            }
        }
    }
    private void insertInterceptor(PhaseHolder phase, PhaseInterceptor interc, boolean force) {
        InterceptorHolder ih = new InterceptorHolder(interc, phase);
        if (phase.head == null) {
            phase.head = ih;
            phase.tail = ih;
            
            PhaseHolder prev = phase.prev;
            while (prev != null 
                && prev.tail == null) {
                prev = prev.prev;
            }
            if (prev != null) {
                //found something before us
                ih.prev = prev.tail;
                ih.next = prev.tail.next;
                if (ih.next != null) {
                    ih.next.prev = ih;
                }
                prev.tail.next = ih;
            } else {
                //did not find something before us, try after
                prev = phase.next;
                while (prev != null 
                    && prev.head == null) {
                    prev = prev.next;
                }
                if (prev != null) {
                    //found something after us
                    ih.next = prev.head;
                    prev.head.prev = ih;
                }
            }
            phase.hasAfters = !interc.getAfter().isEmpty();
            if (iterator != null) {
                outputChainToLog(true);
            }
            return;
        }
        
        Set beforeList = interc.getBefore();
        Set afterList = interc.getAfter();
        InterceptorHolder before = null;
        InterceptorHolder after = null;
        
        String id = interc.getId();
        if (phase.hasAfters
            || !beforeList.isEmpty()) {
        
            InterceptorHolder ih2 = phase.head;
            while (ih2 != phase.tail.next) {
                PhaseInterceptor cmp = ih2.interceptor;
                String cmpId = cmp.getId();
                if (cmpId != null
                    && before == null
                    && (beforeList.contains(cmpId)
                        || cmp.getAfter().contains(id))) {
                    //first one we need to be before
                    before = ih2;
                } 
                if (cmpId != null 
                    && afterList.contains(cmpId)) {
                    after = ih2;
                }
                if (!force
                    && cmpId.equals(id)) {
                    return;
                }
                ih2 = ih2.next;
            }
            if (after == null
                && beforeList.contains("*")) {
                before = phase.head;
            }
            //System.out.print("Didn't skip: " + phase.toString());
            //System.out.println("             " + interc.getId());
        } else if (!force) {
            InterceptorHolder ih2 = phase.head;
            while (ih2 != phase.tail.next) {
                PhaseInterceptor cmp = ih2.interceptor;
                String cmpId = cmp.getId();
                if (!force
                    && cmpId.equals(id)) {
                    return;
                }
                ih2 = ih2.next;
            }
            
            //System.out.print("Skipped: " + phase.toString());
            //System.out.println("         " + interc.getId());
        }
        phase.hasAfters |= afterList.isEmpty();
        
        if (before == null) {
            //just add at the end
            ih.prev = phase.tail;
            if (phase.tail != null) {
                ih.next = phase.tail.next;
                phase.tail.next = ih;
            }
            if (ih.next != null) {
                ih.next.prev = ih;
            }
            phase.tail = ih;
        } else {
            ih.prev = before.prev;
            if (ih.prev != null) {
                ih.prev.next = ih;
            }
            ih.next = before;
            before.prev = ih;
            
            if (phase.head == before) {
                phase.head = ih;
            }
        }
        if (iterator != null) {
            outputChainToLog(true);
        }
    }

    public String toString() {
        return toString(""); 
    }
    private String toString(String message) {
        StringBuilder chain = new StringBuilder();
        
        chain.append("Chain ")
            .append(super.toString())
            .append(message)
            .append(". Current flow:\n");
        
        PhaseHolder ph = firstPhase;
        while (ph != null) {
            if (ph.head != null) {
                chain.append("  ");
                printPhase(ph, chain);
            }
            ph = ph.next;
        }
        return chain.toString();
    }
    private static void printPhase(PhaseHolder ph, StringBuilder chain) {
        chain.append(ph.phase.getName())
            .append(" [");
        InterceptorHolder i = ph.head;
        boolean first = true;
        while (i != ph.tail.next) {
            if (first) {
                first = false;
            } else {
                chain.append(", ");
            }
            chain.append(i.interceptor.getClass().getSimpleName());
            i = i.next;
        }
        chain.append("]\n");
    }
    
    private void outputChainToLog(boolean modified) {
        if (LOG.isLoggable(Level.FINE)) {
            if (modified) {
                LOG.fine(toString(" was modified"));
            } else {
                LOG.fine(toString(" was created"));
            }
        }
    }
    
    public MessageObserver getFaultObserver() {
        return faultObserver;
    }
    
    public void setFaultObserver(MessageObserver faultObserver) {
        this.faultObserver = faultObserver;
    }
    
    static final class PhaseInterceptorIterator implements ListIterator<Interceptor<? extends Message>> {
        PhaseHolder firstPhase;
        InterceptorHolder prev;
        InterceptorHolder first;
        
        public PhaseInterceptorIterator(PhaseHolder f) {
            firstPhase = f;
            first = findFirst();
        }
        
        public void reset() {
            prev = null;
            first = findFirst();
        }
        
        private InterceptorHolder findFirst() {
            PhaseHolder ph = firstPhase;
            while (ph != null && ph.head == null) {
                ph = ph.next;
            }
            if (ph != null) {
                return ph.head;
            }
            return null;
        }
        
        
        public boolean hasNext() {
            if (prev == null) {
                return first != null;
            }
            return prev.next != null;
        }

        @SuppressWarnings("unchecked")
        public Interceptor<? extends Message> next() {
            if (prev == null) {
                if (first == null) {
                    throw new NoSuchElementException();
                }
                prev = first;
            } else {
                if (prev.next == null) {
                    throw new NoSuchElementException();
                }
                prev = prev.next;
            }
            return prev.interceptor;
        }
        public InterceptorHolder nextInterceptorHolder() {
            if (prev == null) {
                if (first == null) {
                    throw new NoSuchElementException();
                }
                prev = first;
            } else {
                if (prev.next == null) {
                    throw new NoSuchElementException();
                }
                prev = prev.next;
            }
            return prev;
        }
        
        public boolean hasPrevious() {
            return prev != null;
        }
        @SuppressWarnings("unchecked")
        public Interceptor<? extends Message> previous() {
            if (prev == null) {
                throw new NoSuchElementException();
            }
            InterceptorHolder tmp = prev;
            prev = prev.prev;
            return tmp.interceptor;
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
        public void set(Interceptor o) {
            throw new UnsupportedOperationException();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }



    
    
    static final class InterceptorHolder {
        PhaseInterceptor interceptor;
        InterceptorHolder next;
        InterceptorHolder prev;
        PhaseHolder phase;
        
        InterceptorHolder(PhaseInterceptor i, PhaseHolder p) {
            interceptor = i;
            phase = p;
        }
    }
    
    static final class PhaseHolder implements Comparable {
        Phase phase;
        PhaseHolder next;
        PhaseHolder prev;
        
        InterceptorHolder head;
        InterceptorHolder tail;
        boolean hasAfters;
        
        PhaseHolder(Phase p) {
            phase = p;
        }

        public int compareTo(Object o) {
            return phase.compareTo(((PhaseHolder)o).phase);
        }
        public String toString() {
            StringBuilder builder = new StringBuilder();
            printPhase(this, builder);
            return builder.toString();
        }
    }

}
