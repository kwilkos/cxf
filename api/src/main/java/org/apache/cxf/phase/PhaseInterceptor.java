package org.apache.cxf.phase;

import java.util.Set;

import org.apache.cxf.interceptors.Interceptor;
import org.apache.cxf.message.Message;

/**
 * A phase interceptor participates in a PhaseInterceptorChain.
 * <pre>
 * The before and after properties contain a list of Ids that the
 * particular interceptor runs before or after.
 * </pre> 
 * @see org.apache.cxf.phase.PhaseInterceptorChain
 * @author Dan Diephouse
 */
public interface PhaseInterceptor<T extends Message> extends Interceptor<T> {

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
