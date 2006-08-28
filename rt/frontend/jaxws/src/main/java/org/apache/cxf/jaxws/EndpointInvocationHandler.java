package org.apache.cxf.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassInInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassOutInterceptor;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;

public final class EndpointInvocationHandler extends BindingProviderImpl implements InvocationHandler {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);

    // private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Endpoint endpoint;

    private Client client;

    private Map<Method, BindingOperationInfo> infoMap = new ConcurrentHashMap<Method, BindingOperationInfo>();

    EndpointInvocationHandler(Client c, Binding b) {
        super(b);
        endpoint = c.getEndpoint();
        client = c;
        client.getOutInterceptors().add(new WrapperClassOutInterceptor());
        client.getInInterceptors().add(new WrapperClassInInterceptor());
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        BindingOperationInfo oi = getOperationInfo(proxy, method);

        // REVISIT - Holder objects, etc...

        Object[] params = args;
        if (null == params) {
            params = new Object[0];
        }
        Object obj[] = client.invoke(oi, params, null);

        return obj.length == 0 ? null : obj[0];
    }

    BindingOperationInfo getOperationInfo(Object proxy, Method method) {
        // TODO: We can't really just associate a method with the operationInfo
        // by its name. The operation name in the wsdl might be something
        // different.
        // For instance, if we have two methods named Foo, there might bee Foo1
        // and Foo2 since the WS-I BP disallows operations with the same name.

        BindingOperationInfo boi = infoMap.get(method);

        if (null == boi) {
            WebMethod wma = method.getAnnotation(WebMethod.class);
            String operationName = null;
            if (null != wma && !"".equals(wma.operationName())) {
                operationName = wma.operationName();
            } else {
                operationName = method.getName();
            }

            InterfaceInfo ii = endpoint.getService().getServiceInfo().getInterface();
            QName oiQName = new QName(endpoint.getService().getName().getNamespaceURI(), operationName);
            OperationInfo oi = ii.getOperation(oiQName);
            if (null == oi) {
                Message msg = new Message("NO_OPERATION_INFO", LOG, operationName);
                throw new WebServiceException(msg.toString());
            }
            // found the OI in the Interface, now find it in the binding
            BindingOperationInfo boi2 = endpoint.getEndpointInfo().getBinding().getOperation(oiQName);
            if (boi2.getOperationInfo() == oi) {
                if (boi2.isUnwrappedCapable()) {
                    try {
                        Class requestWrapper = getRequestWrapper(method);
                        Class responseWrapper = getResponseWrapper(method);

                        if (requestWrapper != null || responseWrapper != null) {
                            BindingOperationInfo boi3 = boi2.getUnwrappedOperation();
                            oi = boi3.getOperationInfo();
                            oi.setProperty(WrapperClassOutInterceptor.SINGLE_WRAPPED_PART, requestWrapper);
                            boi2.getOperationInfo().setProperty(WrappedInInterceptor.SINGLE_WRAPPED_PART,
                                            Boolean.TRUE);
                            infoMap.put(method, boi3);
                            return boi3;
                        }
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                        // TODO - exception
                    }
                }
                infoMap.put(method, boi2);
                return boi2;
            }
        }
        return boi;
    }

    protected Class getResponseWrapper(Method selected) throws ClassNotFoundException {
        ResponseWrapper rw = selected.getAnnotation(ResponseWrapper.class);
        if (rw == null) {
            return null;
        }
        String cn = rw.className();
        return ClassLoaderUtils.loadClass(cn, selected.getDeclaringClass());
    }

    protected Class getRequestWrapper(Method selected) throws ClassNotFoundException {
        RequestWrapper rw = selected.getAnnotation(RequestWrapper.class);
        if (rw == null) {
            return null;
        }
        String cn = rw.className();
        return ClassLoaderUtils.loadClass(cn, selected.getDeclaringClass());
    }

}
