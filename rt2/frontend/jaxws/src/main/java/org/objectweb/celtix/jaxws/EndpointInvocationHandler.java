package org.objectweb.celtix.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.endpoint.Client;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public final class EndpointInvocationHandler extends BindingProviderImpl implements InvocationHandler {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);
    // private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Endpoint endpoint;
    private Client client;

    private Map<Method, OperationInfo> infoMap = new ConcurrentHashMap<Method, OperationInfo>();

    EndpointInvocationHandler(Client c, Binding b) {
        super(b);
        endpoint = c.getEndpoint();
        client = c;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        OperationInfo oi = getOperationInfo(proxy, method);

        // REVISIT - Holder objects, etc...
        Object obj[] = client.invoke(oi, args, null);

        return obj.length == 0 ? null : obj[0];
    }

    OperationInfo getOperationInfo(Object proxy, Method method) {
        // TODO: We can't really just associate a method with the operationInfo
        // by its name. The operation name in the wsdl might be something
        // different.
        // For instance, if we have two methods named Foo, there might bee Foo1
        // and Foo2 since the WS-I BP disallows operations with the same name.

        OperationInfo oi = infoMap.get(method);

        if (null == oi) {
            WebMethod wma = method.getAnnotation(WebMethod.class);
            String operationName = null;
            if (null != wma && !"".equals(wma.operationName())) {
                operationName = wma.operationName();
            } else {
                operationName = method.getName();
            }

            InterfaceInfo ii = endpoint.getService().getServiceInfo().getInterface();
   
            oi = ii.getOperation(new QName(endpoint.getService().getName().getNamespaceURI(), operationName));
            if (null == oi) {
                Message msg = new Message("NO_OPERATION_INFO", LOG, operationName);
                throw new WebServiceException(msg.toString());
            }
            infoMap.put(method, oi);
        }
        return oi;
    }

}
