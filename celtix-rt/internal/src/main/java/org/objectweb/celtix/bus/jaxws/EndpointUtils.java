package org.objectweb.celtix.bus.jaxws;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public final class EndpointUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointUtils.class);

    private EndpointUtils() {
        // Utility class - never constructed
    }

    public static ServiceMode getServiceMode(EndpointImpl endpoint) {
        assert null != endpoint;
        Class<?> implementorClass = endpoint.getImplementorClass();
        if (implementorClass.isAssignableFrom(Provider.class)) {
            return implementorClass.getAnnotation(ServiceMode.class);
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

    public static Method getMethod(EndpointImpl endpoint, QName operationName) {
        Class<?> iClass = endpoint.getImplementorClass();
        return getMethod(iClass, operationName);
    }
    public static Method getMethod(Class<?> iClass, QName operationName) {   
        // determine the (fully annoated) SEI
        List<Class<?>> list = getWebServiceAnnotatedClass(iClass);

        // determine the method in the SEI
        String methodName = operationName.getLocalPart();
        Method iMethod = null;
        
        Iterator<Class<?>> iter = list.iterator();
        boolean strictMatch = false;
        while (iter.hasNext() && !strictMatch) {
            Class<?> sei = iter.next();
            Method[] iMethods = sei.getMethods();

            for (Method m : iMethods) {
                WebMethod wm = m.getAnnotation(WebMethod.class);

                if (wm != null && !"".equals(wm.operationName())) {
                    if (methodName.equals(wm.operationName()) 
                        && methodName.equalsIgnoreCase(m.getName())) {
                        iMethod = m;
                        strictMatch = true;
                        break;
                    }
                } else if (methodName.equals(m.getName())) {
                    iMethod = m;
                    break;
                }
            }
        }
        
        if (null == iMethod) {
            LOG.log(Level.SEVERE, "METHOD_NOT_DEFINED_MSG", methodName);
            return null;
        }

        return iMethod;
    }

    public static Class<?> getProviderParameterType(EndpointImpl endpoint) {
        //The Provider Implementor inherits out of Provier<T>
        Type intfTypes[] = endpoint.getImplementorClass().getGenericInterfaces();
        for (Type t : intfTypes) {
            Class<?> clazz = JAXBEncoderDecoder.getClassFromType(t);
            if (Provider.class == clazz) {
                Type paramTypes[] = ((ParameterizedType)t).getActualTypeArguments();
                return JAXBEncoderDecoder.getClassFromType(paramTypes[0]);
            }
        }
        
        return null;
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
    
    public static List<Class<?>> getWebServiceAnnotatedClass(Class<?> cls) {
        List<Class<?>> list = new ArrayList<Class<?>>();

        Class<?>[] interfaces = cls.getInterfaces();
        for (Class<?> c : interfaces) {
            list.addAll(getWebServiceAnnotatedClass(c));
        }
        
        if (!cls.isInterface()) {
            Class<?> superClass = cls.getSuperclass();        
            if (superClass != null) {
                list.addAll(getWebServiceAnnotatedClass(superClass));
            }
        }
        
        if (cls.isAnnotationPresent(WebService.class)) {
            list.add(cls);
        }                
        
        return list;        
    }
    
    
}
