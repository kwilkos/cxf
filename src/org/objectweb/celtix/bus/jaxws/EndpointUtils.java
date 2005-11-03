package org.objectweb.celtix.bus.jaxws;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public final class EndpointUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointUtils.class);

    private EndpointUtils() {
        // Utility class - never constructed
    }

    public static ServiceMode getServiceMode(Endpoint endpoint) {
        assert null != endpoint;
        Object implementor = endpoint.getImplementor();
        if (implementor instanceof Provider) {
            return implementor.getClass().getAnnotation(ServiceMode.class);
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
        WebService iws = EndpointReferenceUtils.getWebServiceAnnotation(iClass);

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
            LOG.severe("WEBSERVICE_ANNOTATION_NOT_PRESENT_MSG");
            return null;
        }
        
        if (null == sei) {
            LOG.severe("SEI_NOT_IMPLEMENTED_MSG");
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
            LOG.log(Level.SEVERE, "METHOD_NOT_DEFINED_MSG", methodName);
            return null;
        }

        return iMethod;
    }

    private static boolean hasWebServiceAnnotation(Class<?> cls) {
        if (cls == null) {
            return false;
        }
        if (null != cls.getAnnotation(WebService.class)) {
            return true;
        }
        for (Class<?> inf : cls.getInterfaces()) {
            if (null != inf.getAnnotation(WebService.class)) {
                return true;
            }
        }
        
        return hasWebServiceAnnotation(cls.getSuperclass());
    }
    public static boolean isValidImplementor(Object implementor) {
        if (implementor instanceof Provider) {
            return true;
        }

        // implementor MUST be an instance of a class with a WebService
        // annotation
        // (that implements an SEI) OR a Provider

        if (hasWebServiceAnnotation(implementor.getClass())) {
            return true;
        }

        LOG.info("Implementor is not annotated with WebService annotation.");
        return false;
    }
}
