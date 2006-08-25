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
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;

import org.objectweb.celtix.common.logging.LogUtils;

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

        Class<?> iClass = endpoint.getImplementor().getClass();

        // determine the (fully annoated) SEI
        List<Class<?>> list = getWebServiceAnnotatedClass(iClass);
        //Add the Implementor incase there is no SEI.
        if (list.size() == 0) {
            list.add(iClass);
        }
        // determine the method in the SEI

        String methodName = operationName.getLocalPart();
        Method iMethod = null;
        
        Iterator<Class<?>> iter = list.iterator();
        
        while (iter.hasNext()) {
            Class<?> sei = iter.next();
            Method[] iMethods = sei.getMethods();

            for (Method m : iMethods) {
                WebMethod wm = m.getAnnotation(WebMethod.class);

                if (wm != null && !"".equals(wm.operationName())) {
                    if (methodName.equals(wm.operationName()) 
                        && methodName.equalsIgnoreCase(m.getName())) {
                        iMethod = m;
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
    
    public static Class<?> getProviderParameterType(Endpoint endpoint) {
        //The Provider Implementor inherits out of Provier<T>
        Type intfTypes[] = endpoint.getImplementor().getClass().getGenericInterfaces();
        Type providerType = null;
        for (Type t : intfTypes) {
            Class<?> clazz = JAXBEncoderDecoder.getClassFromType(t);
            if (Provider.class == clazz) {
                providerType = t;
            }
        }
        Type paramTypes[] = ((ParameterizedType)providerType).getActualTypeArguments();
        return JAXBEncoderDecoder.getClassFromType(paramTypes[0]);       
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
    
    private static List<Class<?>> getWebServiceAnnotatedClass(Class<?> cls) {
        List<Class<?>> list = new ArrayList<Class<?>>();

        if (!cls.isInterface()) {
            Class<?> superClass = cls.getSuperclass();        
            if (superClass != null) {
                if (superClass.isAnnotationPresent(WebService.class)) {
                    list.add(superClass);
                }
                list.addAll(getWebServiceAnnotatedClass(superClass));
            }
        }
        
        Class<?>[] interfaces = cls.getInterfaces();
        for (Class<?> c : interfaces) {
            if (c.isAnnotationPresent(WebService.class)) {
                list.add(c);
            }
            list.addAll(getWebServiceAnnotatedClass(c));
        }

        return list;        
    }
    
    
}
