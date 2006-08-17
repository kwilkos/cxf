package org.objectweb.celtix.jaxws.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.objectweb.celtix.common.classloader.ClassLoaderUtils;
import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.resource.URIResolver;
import org.objectweb.celtix.service.factory.AbstractServiceConfiguration;
import org.objectweb.celtix.service.factory.ServiceConstructionException;
import org.objectweb.celtix.service.model.InterfaceInfo;

public class JaxWsServiceConfiguration extends AbstractServiceConfiguration {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JaxWsServiceConfiguration.class);

    private Class endpointInterface;

    WebService getConcreteWebServiceAttribute() {
        return getServiceFactory().getServiceClass().getAnnotation(WebService.class);
    }

    WebService getPortTypeWebServiceAttribute() {
        Class<?> epi = getEndpointClass();
        if (epi != null) {
            epi.getAnnotation(WebService.class);
        }
        return getServiceFactory().getServiceClass().getAnnotation(WebService.class);
    }

    Class getEndpointClass() {
        if (endpointInterface == null) {
            String wsClassName = getConcreteWebServiceAttribute().endpointInterface();

            if (wsClassName.length() == 0) {
                return getServiceFactory().getServiceClass();
            }

            try {
                endpointInterface = ClassLoaderUtils.loadClass(wsClassName, getClass());
            } catch (ClassNotFoundException e) {
                throw new ServiceConstructionException(new Message("INTERFACE_LOAD_EXC", 
                                                                   BUNDLE, 
                                                                   wsClassName),
                                                       e);
            }
        }

        if (endpointInterface == null) {
            return getServiceFactory().getServiceClass();
        }

        return endpointInterface;
    }

    @Override
    public String getServiceName() {
        WebService ws = getConcreteWebServiceAttribute();
        if (ws != null) {
            return ws.serviceName();
        }

        return null;
    }

    @Override
    public String getServiceNamespace() {
        WebService ws = getConcreteWebServiceAttribute();
        if (ws != null) {
            return ws.targetNamespace();
        }

        return null;
    }

    @Override
    public URL getWsdlURL() {
        WebService ws = getPortTypeWebServiceAttribute();
        if (ws != null && ws.wsdlLocation().length() > 0) {
            try {
                URIResolver resolver = new URIResolver(ws.wsdlLocation());
                if (resolver.isResolved()) {
                    resolver.getURI().toURL();
                }
            } catch (IOException e) {
                throw new ServiceConstructionException(new Message("LOAD_WSDL_EXC", 
                                                                   BUNDLE, 
                                                                   ws.wsdlLocation()),
                                                       e);
            }
        }
        return null;
    }

    @Override
    public QName getOperationName(InterfaceInfo service, Method method) {
        method = getDeclaredMethod(method);

        WebMethod wm = method.getAnnotation(WebMethod.class);
        if (wm != null) {
            String name = wm.operationName();
            if (name == null) {
                name = method.getName();
            }

            return new QName(service.getName().getNamespaceURI(), name);
        }

        return null;
    }

    @Override
    public Boolean isOperation(Method method) {
        method = getDeclaredMethod(method);
        if (method != null) {
            WebMethod wm = method.getAnnotation(WebMethod.class);
            if (wm != null) {
                if (wm.exclude()) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if (!method.getDeclaringClass().isInterface()) {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    private Method getDeclaredMethod(Method method) {
        Class endpointClass = getEndpointClass();

        if (!method.getDeclaringClass().equals(endpointClass)) {
            try {
                method = endpointClass.getMethod(method.getName(), method.getParameterTypes());
            } catch (SecurityException e) {
                throw new ServiceConstructionException(e);
            } catch (NoSuchMethodException e) {
                // Do nothing
            }
        }
        return method;
    }

}
