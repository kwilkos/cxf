package org.objectweb.celtix.service.invoker;

import org.objectweb.celtix.message.Exchange;

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
