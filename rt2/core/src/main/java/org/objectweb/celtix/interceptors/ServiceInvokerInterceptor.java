package org.objectweb.celtix.interceptors;

import java.util.concurrent.Executor;

import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.invoker.Invoker;

/**
 * Invokes a Binding's invoker with the <code>INVOCATION_INPUT</code> from
 * the Exchange.
 * @author Dan Diephouse
 */
public class ServiceInvokerInterceptor extends AbstractPhaseInterceptor<Message> {
    
    /** The property which contains the input for the Invoker in the Exchange. */
    public static final String INVOCATION_INPUT = "cxf.invoker.in";
    
    /** The property which contains the output for the Invoker in the Exchange. */
    public static final String INVOCATION_OUTPUT = "cxf.invoker.out";

    public void handleMessage(final Message message) {
        final Exchange exchange = message.getExchange();
        final Endpoint endpoint = (Endpoint)exchange.get(ExchangeConstants.ENDPOINT);
        final Service service = endpoint.getService();
        final Invoker invoker = service.getInvoker();        

        getExecutor(endpoint).execute(new Runnable() {

            public void run() {
                Object result = invoker.invoke(message.getExchange(), exchange.get(INVOCATION_INPUT));

                exchange.put(INVOCATION_OUTPUT, result);
            }

        });
    }

    /**
     * Get the Executor for this invocation.
     * @param endpoint
     * @return
     */
    private Executor getExecutor(final Endpoint endpoint) {
        return endpoint.getService().getExecutor();
    }
}
