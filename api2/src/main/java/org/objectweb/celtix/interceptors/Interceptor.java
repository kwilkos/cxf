package org.objectweb.celtix.interceptors;

import org.objectweb.celtix.message.Message;

public interface Interceptor<T extends Message> {
    /**
     * Intercepts a message. At some point in the intercept method
     * the next interceptor must be invoked:
     * <pre>
     * message.getInterceptorChain().doIntercept(message);
     * </pre>
     * This also allows one to replace the message being used:
     * <pre>
     * SoapMessage soapMessage = new SoapMessage(message);
     * ... act on the message ...
     * message.getInterceptorChain().doIntercept(soapMessage);
     * </pre>
     * @param message
     */
    void intercept(T message);
}
