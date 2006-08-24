package org.apache.cxf.service.invoker;

import org.apache.cxf.message.Exchange;

/**
 * Invokers control how a particular service is invoked. It may maintain
 * state or it may create new service objects for every request. It is
 * up to the invoker.
 * 
 * @author Dan Diephouse
 */
public interface Invoker {

    Object invoke(Exchange exchange, Object o);
}
