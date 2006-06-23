package org.objectweb.celtix.rio.examples;

import java.util.concurrent.Executor;

import org.objectweb.celtix.rio.Interceptor;
import org.objectweb.celtix.rio.Message;

public class PriorityInterceptor implements Interceptor {

    public void intercept(final Message message) {
        Runnable runnable = new Runnable() {
            public void run() {
                message.getInterceptorChain().doIntercept(message);
            }
        };

        if (isHighPriority(message)) {
            getHighPrioirityExectutor().execute(runnable);
        } else {
            getLowPrioirityExectutor().execute(runnable);
        }
    }

    private boolean isHighPriority(Message message) {
        // TODO Auto-generated method stub
        return false;
    }

    private Executor getLowPrioirityExectutor() {
        return null;
    }
    
    private Executor getHighPrioirityExectutor() {
        return null;
    }
}
