package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.concurrent.Future;

import org.objectweb.celtix.context.ObjectMessageContext;

/**
 * Binding
 * @author dkulp
 *
 */
public interface ClientBinding extends BindingBase {

    void invokeOneWay(ObjectMessageContext context) throws IOException;

    ObjectMessageContext invoke(ObjectMessageContext context) throws IOException;

    Future<ObjectMessageContext> invokeAsync(ObjectMessageContext context) throws IOException;
}
