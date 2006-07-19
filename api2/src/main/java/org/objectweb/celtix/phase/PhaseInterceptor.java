package org.objectweb.celtix.phase;

import java.util.Set;

import org.objectweb.celtix.interceptors.Interceptor;

/**
 * A phase interceptor participates in a PhaseInterceptorChain.
 * <pre>
 * The before and after properties contain a list of Ids that the
 * particular interceptor runs before or after.
 * </pre> 
 * @see org.objectweb.celtix.phase.PhaseInterceptorChain
 * @author Dan Diephouse
 */
public interface PhaseInterceptor extends Interceptor {

    /**
     * A Set of IDs that this interceptor needs to run after.
     * @return
     */
    Set<String> getAfter();

    /**
     * A Set of IDs that this interceptor needs to run before.
     * @return
     */
    Set<String> getBefore();

    /**
     * The ID of the interceptor.
     * @return
     */
    String getId();

    String getPhase();

}
