package org.objectweb.celtix.bindings;

import java.util.concurrent.Future;

import org.objectweb.celtix.context.ObjectMessageContext;

/**
 * Binding
 * @author dkulp
 *
 */
public interface ClientBinding {

    ObjectMessageContext createObjectContext();



    void invokeOneWay(ObjectMessageContext context);

    ObjectMessageContext invoke(ObjectMessageContext context);

    Future<ObjectMessageContext> invokeAsync(ObjectMessageContext context);
}
