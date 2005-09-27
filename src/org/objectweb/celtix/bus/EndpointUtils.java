package org.objectweb.celtix.bus;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;

public final class EndpointUtils {

    private static final Logger LOG = Logger.getLogger(EndpointUtils.class.getName());

    private EndpointUtils() {
        // Utility class - never constructed
    }

    public static ServiceMode getServiceMode(Endpoint endpoint) {
        assert null != endpoint;
        Object implementor = endpoint.getImplementor();
        if (implementor instanceof Provider) {
            ServiceMode mode = implementor.getClass().getAnnotation(ServiceMode.class);
            return mode;
        }
        return null;
    }

    /**
     * Returns the method in the <code>Endpoint</code>'s implementor that
     * implements the specified operation. This assumes that the
     * <code>Endpoint</code>'s implementor is annotated with the
     * <code>WebService</code> annotation. The implementor's (public) methods
     * need not necessarily be annotated. REVISIT: Does the implementor have to
     * implement an SEI, or does it even have to implement the Remote interface?
     * 
     * @param endpoint
     * @param operationName
     * @return the <code>Method</code> in the <code>Endpoint</code>'s implementor.
     */

    public static Method getMethod(Endpoint endpoint, QName operationName) {
        Object implementor = endpoint.getImplementor();
        Class<?> iClass = implementor.getClass();
        WebService iws = iClass.getAnnotation(WebService.class);

        // determine the (fully annoated) SEI

        Class<?>[] interfaces = iClass.getInterfaces();

        Class<?> sei = null;
        for (Class<?> c : interfaces) {
            WebService ws = c.getAnnotation(WebService.class);
            // REVISIT: check for equality of targetNamespace also
            if (null != ws) {                
                sei = c;
                if (null == iws) {
                    iws = ws;
                }
                break;
            }
        }
        
        if (null == iws) {
            LOG.severe("Implementor or SEI is not annotated with WebService annotation.");
            return null;
        }
        
        if (null == sei) {
            LOG.severe("Implementor does not implement required SEI.");
            return null;
        }

        // determine the method in the SEI

        String methodName = operationName.getLocalPart();

        Method[] iMethods = sei.getMethods();
        Method iMethod = null;
        for (Method m : iMethods) {
            if (m.getName().equals(methodName)) {
                iMethod = m;
                WebMethod wm = m.getAnnotation(WebMethod.class);
                if (wm != null && wm.operationName().equals(methodName)) {
                    break;
                }
                // assume this is an overloaded version of the method we are
                // looking for
                // continue searching for a better match
            }
        }

        if (null == iMethod) {
            LOG.severe("Method " + methodName + " is not defined in SEI.");
            return null;
        }

        return iMethod;
    }

    public static boolean isValidImplementor(Object implementor) {
        if (implementor instanceof Provider) {
            return true;
        }

        // implementor MUST be an instance of a class with a WebService
        // annotation
        // (that implements an SEI) OR a Provider

        if (null == implementor.getClass().getAnnotation(WebService.class)) {
            LOG.info("Implementor is not annotated with WebService annotation.");
            return false;
        }

        return true;
    }
}
