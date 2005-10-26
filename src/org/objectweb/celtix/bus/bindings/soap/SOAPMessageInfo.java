package org.objectweb.celtix.bus.bindings.soap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

public class SOAPMessageInfo {
    private SOAPBinding soapBindAnnotation;
    private WebMethod webMethodAnnotation;
    private WebResult webResultAnnotation;
    private Annotation[][] paramAnnotations;
    private RequestWrapper reqWrapper;
    private ResponseWrapper respWrapper;
    private Method method;
    private WebService webServiceAnnotation;

    public SOAPMessageInfo(Method m) {
        method = m;
        init();
    }

    private void init() {
        //Get WebService Annotation.
        webServiceAnnotation = method.getDeclaringClass().getAnnotation(WebService.class);
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

        if (null != webMethodAnnotation &&  !"".equals(webMethodAnnotation.operationName())) {
            return webMethodAnnotation.operationName();
        }
        return getMethod().getName();
    }

    public String getSOAPAction() {
        if (null != webMethodAnnotation) {
            return webMethodAnnotation.action();
        }
        return "";
    }

    public QName getWebResult() {
        if (null != webResultAnnotation) {
            if (getSOAPStyle() == Style.DOCUMENT && getSOAPParameterStyle() == ParameterStyle.WRAPPED) {
                return new QName(webResultAnnotation.targetNamespace(),
                                 webResultAnnotation.name());
            } else {
                // RPC-Lit case.
                //REVISIT : For spec. compliance with other types. 
                return new QName(null, webResultAnnotation.partName());
            }
        }
        return SOAPConstants.EMPTY_QNAME;
    }
    
    public WebResult getWebResultAnnotation() {
        return webResultAnnotation;
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
    
    public WebService getWebService() {
        return webServiceAnnotation;
    }
    
    public String getTargetNameSpace() {
        return webServiceAnnotation.targetNamespace();
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
    
    public Class<?> getWebFault(QName faultName) {
        for (Class<?> clazz : getMethod().getExceptionTypes()) {
            WebFault wfAnnotation = clazz.getAnnotation(WebFault.class);
            if (wfAnnotation != null
                && (wfAnnotation.name().equals(faultName.getLocalPart()) 
                && wfAnnotation.targetNamespace().equals(faultName.getNamespaceURI()))) {
                return clazz;
            }
        }
        return null;
    }
}
