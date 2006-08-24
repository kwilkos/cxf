package org.apache.cxf.phase;

import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.message.Message;

public abstract class AbstractPhaseInterceptor<T extends Message> implements PhaseInterceptor<T> {
    private String id;
    private String phase;
    private Set<String> before = new HashSet<String>();
    private Set<String> after = new HashSet<String>();

    
    public AbstractPhaseInterceptor() {
        super();
        id = getClass().getName();
    }

    public void addBefore(String i) {
        before.add(i);
    }

    public void addAfter(String i) {
        after.add(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cxf.phase.PhaseInterceptor#getAfter()
     */
    public Set<String> getAfter() {
        return after;
    }

    public void setAfter(Set<String> a) {
        this.after = a;
    }

    public Set<String> getBefore() {
        return before;
    }

    public void setBefore(Set<String> b) {
        this.before = b;
    }

    public String getId() {
        return id;
    }

    public void setId(String i) {
        this.id = i;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String p) {
        this.phase = p;
    }

    public void handleFault(T message) {
    }
}
