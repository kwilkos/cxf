package org.objectweb.celtix.service.invoker;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.objectweb.celtix.interceptors.Fault;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.service.model.BindingOperationInfo;

/**
 * An invoker which invokes a bean method. Will be replaced soon with something better.
 */
public class SimpleMethodInvoker implements Invoker {
    private Object bean;
    
    public SimpleMethodInvoker(Object bean) {
        super();
        this.bean = bean;
    }

    public Object invoke(Exchange exchange, Object o) {
        BindingOperationInfo bop = (BindingOperationInfo) exchange.get(BindingOperationInfo.class.getName());
        
        Method m = (Method) bop.getOperationInfo().getProperty(Method.class.getName());
        List<?> params = (List<?>) o;
        
        Object res;
        try {
            res = m.invoke(bean, params.toArray()); 
            return Arrays.asList(res);
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

}
