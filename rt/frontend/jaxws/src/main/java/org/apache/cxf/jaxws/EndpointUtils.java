package org.apache.cxf.jaxws;

import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

import org.apache.cxf.common.logging.LogUtils;

public final class EndpointUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointUtils.class);

    private EndpointUtils() {
        // Utility class - never constructed
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
    
    private static boolean hasWebServiceProviderAnnotation(Class<?> cls) {
        if (cls != null) {
            return cls.isAnnotationPresent(WebServiceProvider.class);
        }
        
        return false;
    }
    
    public static boolean isValidImplementor(Object implementor) {
        if (Provider.class.isAssignableFrom(implementor.getClass())
            && hasWebServiceProviderAnnotation(implementor.getClass())) {
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
