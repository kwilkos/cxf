package org.objectweb.celtix.bus.bindings.soap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPMessageInfo {
    private SOAPBinding soapBindAnnotation;
    private WebMethod webMethodAnnotation;
    private WebResult webResultAnnotation;
    private Annotation[][] paramAnnotations;
    
    public SOAPMessageInfo(ObjectMessageContext msgCtx) {
        init(msgCtx);
    }
    
    private void init(ObjectMessageContext msgCtx) {
        Method method = msgCtx.getMethod();
        //Get SOAP Style, Use, 
        soapBindAnnotation = method.getDeclaringClass().getAnnotation(SOAPBinding.class);
        //Get Operation,Action Info
        webMethodAnnotation = method.getAnnotation(WebMethod.class);
        //Get Parameter Info
        paramAnnotations = method.getParameterAnnotations();
        //Get Return Type Info
        webResultAnnotation = method.getAnnotation(WebResult.class);
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
            return new QName(webResultAnnotation.name(), 
                     webResultAnnotation.targetNamespace());
        }
        return SOAPConstants.EMPTY_QNAME;
    }
    
    public WebParam getWebParam(int index) {
        if (null != paramAnnotations) {
            for (Annotation annotation : paramAnnotations[index]) {
                if (WebParam.class.equals(annotation.annotationType())) {
                    return (WebParam) annotation;
                }
            }
        }
        return null;
    }
}
