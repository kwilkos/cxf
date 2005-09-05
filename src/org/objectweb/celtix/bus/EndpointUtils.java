package org.objectweb.celtix.bus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;

public final class EndpointUtils {

    private static Logger logger = Logger.getLogger(EndpointUtils.class.getName());

    private EndpointUtils() {
        // Utility class - never constructed
    }

    public static ServiceMode getServiceMode(Endpoint endpoint) {
        Object implementor = endpoint.getImplementor();
        if (implementor instanceof Provider) {
            ServiceMode mode = (ServiceMode)implementor.getClass().getAnnotation(ServiceMode.class);
            return mode;
        }
        return null;
    }

    /**
     * Returns the method in the <code>Endpoint</code>'s implementor that
     * implements the specified operation. This assumes that the
     * <code>Endpoint</code>'s implementor is annotated with the
     * <code>WebService</code> annotation. The implementor's (public) methods
     * need not necessarily be annotated. 
     * REVISIT: Does the implementor have to implement an SEI, or does it even
     * have to implement the Remote interface?
     * 
     * @param endpoint
     * @param operationName
     * @return
     */

    public static Method getMethod(Endpoint endpoint, QName operationName) {
        Object implementor = endpoint.getImplementor();
        Class iClass = implementor.getClass();
        WebService iws = (WebService)implementor.getClass().getAnnotation(WebService.class);

        if (null == iws) {
            logger.severe("Implementor is not annotated with WebService annotation.");
            return null;
        }

        // determine the (fully annoated) SEI

        Class[] interfaces = iClass.getInterfaces();

        Class sei = null;
        for (Class c : interfaces) {
            WebService ws = (WebService)c.getAnnotation(WebService.class);
            // REVISIT: check for equality of targetNamespace also
            if (null != ws && ws.name().equals(iws.name())) {
                sei = c;
                break;
            }
        }
        if (null == sei) {
            logger.severe("Implementor does not implement required SEI.");
            return null;
        }

        // determine the method in the SEI

        String methodName = operationName.getLocalPart();

        Method[] iMethods = sei.getMethods();
        Method iMethod = null;
        for (Method m : iMethods) {
            if (m.getName().equals(methodName)) {
                iMethod = m;
                WebMethod wm = (WebMethod)m.getAnnotation(WebMethod.class);
                if (wm != null && wm.operationName().equals(methodName)) {
                    break;
                }
                // assume this is an overloaded version of the method we are
                // looking for
                // continue searching for a better match
            }
        }

        if (null == iMethod) {
            logger.severe("Method " + methodName + " is not defined in SEI.");
            return null;
        }

        // get corresponding method in implementor

        Method method = null;
        Method[] methods = iClass.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(iMethod.getName())
                && Arrays.equals(m.getParameterTypes(), iMethod.getParameterTypes())) {
                method = m;
                break;
            }
        }

        if (null == method) {
            logger.severe("Implementor does not implement method " + methodName + ".");
            return null;
        }

        return method;
    }

    public static boolean isValidImplementor(Object implementor) {
        if (implementor instanceof Provider) {
            return true;          
        }
        
        // implementor MUST be an instance of a class with a WebService annotation 
        // (that implements an SEI) OR a Provider
        
        WebService iws = (WebService)implementor.getClass().getAnnotation(WebService.class);

        if (null == iws) {
            logger.info("Implementor is not annotated with WebService annotation.");
            return false;
        }
        
        return true;
         
    }

}
