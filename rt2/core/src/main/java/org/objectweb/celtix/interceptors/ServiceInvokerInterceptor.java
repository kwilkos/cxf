package org.objectweb.celtix.interceptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.invoker.Invoker;

/**
 * Invokes a Binding's invoker with the <code>INVOCATION_INPUT</code> from
 * the Exchange.
 * @author Dan Diephouse
 */
public class ServiceInvokerInterceptor extends AbstractPhaseInterceptor<Message> {
   
    
    public ServiceInvokerInterceptor() {
        super();
        setPhase(Phase.INVOKE);
    }

    public void handleMessage(final Message message) {
        final Exchange exchange = message.getExchange();
        final Endpoint endpoint = exchange.get(Endpoint.class);
        final Service service = endpoint.getService();
        final Invoker invoker = service.getInvoker();        

        getExecutor(endpoint).execute(new Runnable() {

            public void run() {
                Object result = invoker.invoke(message.getExchange(), 
                    exchange.getInMessage().getContent(Object.class));

                if (result != null) {
                    if (!(result instanceof List)) {
                        if (result.getClass().isArray()) {
                            result = Arrays.asList((Object[])result);
                        } else {
                            List<Object> o = new ArrayList<Object>();
                            o.add(result);
                            result = o;
                        }
                    }
                    exchange.getOutMessage().setContent(List.class, result);
                }
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
