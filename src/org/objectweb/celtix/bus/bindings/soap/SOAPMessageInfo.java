package org.objectweb.celtix.bus.bindings.soap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

public class SOAPMessageInfo {
    private SOAPBinding soapBindAnnotation;
    private WebMethod webMethodAnnotation;
    private WebResult webResultAnnotation;
    private Annotation[][] paramAnnotations;
    private RequestWrapper reqWrapper;
    private ResponseWrapper respWrapper;
    private Method method;
    
    public SOAPMessageInfo(Method m) {
        method = m;
        init();
    }
    
    private void init() {
        //Get SOAP Style, Use, 
        soapBindAnnotation = method.getDeclaringClass().getAnnotation(SOAPBinding.class);
        //Get Operation,Action Info
        webMethodAnnotation = method.getAnnotation(WebMethod.class);
        //Get Parameter Info
        paramAnnotations = method.getParameterAnnotations();
        //Get Return Type Info
        webResultAnnotation = method.getAnnotation(WebResult.class);
        //Get the RequestWrapper
        reqWrapper = method.getAnnotation(RequestWrapper.class);
        //Get the RequestWrapper
        respWrapper = method.getAnnotation(ResponseWrapper.class);
    }
    
    public SOAPBinding.Style getSOAPStyle() {
        if (null != soapBindAnnotation) {
            return soapBindAnnotation.style();
        }
        return SOAPBinding.Style.DOCUMENT;
    }
    
    public SOAPBinding.Use getSOAPUse() {
        if (null != soapBindAnnotation) {
            return soapBindAnnotation.use();
        }
        return SOAPBinding.Use.LITERAL;
    }

    public SOAPBinding.ParameterStyle getSOAPParameterStyle() {
        if (null != soapBindAnnotation) {        
            return soapBindAnnotation.parameterStyle();
        }
        return SOAPBinding.ParameterStyle.WRAPPED;
    }
    
    public String getOperationName() {
        if (null != webMethodAnnotation) {
            return webMethodAnnotation.operationName();
        }
        return "";
    }

    public String getSOAPAction() {
        if (null != webMethodAnnotation) {
            return webMethodAnnotation.action();
        }
        return "";
    }
    
    public QName getWebResult() {
        if (null != webResultAnnotation) {
            return new QName(webResultAnnotation.targetNamespace(),
                             webResultAnnotation.name());
        }
        return SOAPConstants.EMPTY_QNAME;
    }
    
    public WebParam getWebParam(int index) {
        if (null != paramAnnotations && index < paramAnnotations.length) {
            for (Annotation annotation : paramAnnotations[index]) {
                if (WebParam.class.equals(annotation.annotationType())) {
                    return (WebParam) annotation;
                }
            }
        }
        return null;
    }

    public QName getRequestWrapperQName() {
        if (null != reqWrapper) {
            return new QName(reqWrapper.targetNamespace(),
                             reqWrapper.localName());
        }
        return SOAPConstants.EMPTY_QNAME;
    }

    public String getRequestWrapperType() {
        if (null != reqWrapper) {
            return reqWrapper.className();
        }
        return "";
    }
    
    public QName getResponseWrapperQName() {
        if (null != respWrapper) {
            return new QName(respWrapper.targetNamespace(),
                             respWrapper.localName());
        }
        return SOAPConstants.EMPTY_QNAME;
    }

    public String getResponseWrapperType() {
        if (null != respWrapper) {
            return respWrapper.className();
        }
        return "";
    }
    
    public Method getMethod() {
        return method;
    }
}
