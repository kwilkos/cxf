package org.objectweb.celtix.examples;

import java.util.concurrent.Executor;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;

public class PriorityInterceptor implements Interceptor {

    public void handleMessage(final Message message) {
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
    
    public void handleFault(Message message) {
        
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
