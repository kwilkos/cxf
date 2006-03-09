package org.objectweb.celtix.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.xml.ws.RequestWrapper;

public class WSDLHelper {

    public BindingOperation getBindingOperation(Binding binding, String operationName) {
        if (operationName == null) {
            return null;
        }
        List bindingOperations = binding.getBindingOperations();
        for (Iterator iter = bindingOperations.iterator(); iter.hasNext();) {
            BindingOperation bindingOperation = (BindingOperation) iter.next();
            if (operationName.equals(bindingOperation.getName())) {
                return bindingOperation;
            }
        }
        return null;
    }

    public Map getParts(Operation operation, boolean out) {
        Message message = null;
        if (out) {
            Output output = operation.getOutput();
            message = output.getMessage();
        } else {
            Input input = operation.getInput();
            message = input.getMessage();
        }
        return message.getParts() == null ? new HashMap() : message.getParts();
    }

    public SOAPBinding getBindingAnnotationFromClass(List<Class<?>> classList) {
        SOAPBinding sb = null;
        for (Class<?> c : classList) {
            sb = c.getAnnotation(SOAPBinding.class);
            if (null != sb)  {
                break;
            }
        }
        return sb;
    }

    public SOAPBinding getBindingAnnotationFromMethod(Method m) {
        SOAPBinding sb = null;
        if (null != m) {
            sb = m.getAnnotation(SOAPBinding.class);
        }
        return sb;
    }
    
    public WebParam getWebParamAnnotation(Annotation[] pa) {
        WebParam wp = null;
        
        if (null != pa) {
            for (Annotation annotation : pa) {
                if (WebParam.class.equals(annotation.annotationType())) {
                    wp = (WebParam) annotation;
                    break;
                }
            }
        }
        return wp;
    }
    
    public RequestWrapper getRequestWrapperAnnotation(Method m) {
        RequestWrapper rw = null;
        
        if (null != m) {
            rw = m.getAnnotation(RequestWrapper.class);
        }
        return rw;        
    }
}
