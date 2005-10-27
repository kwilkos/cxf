package org.objectweb.celtix.bus.jaxws;

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
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Node;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.bindings.soap.SOAPConstants;
import org.objectweb.celtix.bus.jaxws.io.DetailDataWriter;
import org.objectweb.celtix.bus.jaxws.io.NodeDataReader;
import org.objectweb.celtix.bus.jaxws.io.NodeDataWriter;
import org.objectweb.celtix.bus.jaxws.io.SOAPFaultDataReader;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.helpers.WrapperHelper;

public class JAXBDataBindingCallback implements DataBindingCallback {
    private SOAPBinding soapBindAnnotation;
    private WebMethod webMethodAnnotation;
    private WebResult webResultAnnotation;
    private Annotation[][] paramAnnotations;
    private RequestWrapper reqWrapper;
    private ResponseWrapper respWrapper;
    private Method method;
    private Mode mode;
    private WebService webServiceAnnotation;
    
    public JAXBDataBindingCallback(Method m, Mode md) {
        method = m;
        mode = md;
        init();
    }

    public Mode getMode() {
        return mode;
    }
    public Class<?>[] getSupportedFormats() {
        if (mode == Mode.PARTS) {
            return new Class<?>[] {Node.class, Detail.class, SOAPFault.class};
        }
        // TODO Auto-generated method stub
        return null;
    }

    public <T> DataWriter<T> createWriter(Class<T> cls) {
        if (cls == Node.class) {
            return new NodeDataWriter<T>(this);
        } else if (cls == Detail.class) {
            return new DetailDataWriter<T>(this);
        }
        // TODO Auto-generated method stub
        return null;
    }

    public <T> DataReader<T> createReader(Class<T> cls) {
        if (cls == Node.class) {
            return new NodeDataReader<T>(this);
        } else if (cls == SOAPFault.class) { 
            return new SOAPFaultDataReader<T>(this);
        }
        // TODO Auto-generated method stub
        return null;
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
    public WebResult getWebResultAnnotation() {
        return webResultAnnotation;
    }
    
    public WebService getWebService() {
        return webServiceAnnotation;
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
    public String getTargetNamespace() {
        return webServiceAnnotation.targetNamespace();
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
        if (method.getReturnType() == Void.class) {
            return null;
        }
        if (null != webResultAnnotation) {
            if (getSOAPStyle() == Style.DOCUMENT && getSOAPParameterStyle() == ParameterStyle.WRAPPED) {
                return new QName(webResultAnnotation.targetNamespace(),
                                 webResultAnnotation.name());
            } else {
                // RPC-Lit case.
                //REVISIT : For spec. compliance with other types. 
                return new QName("", webResultAnnotation.partName());
            }
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

    public int getParamsLength() {
        return method.getParameterTypes().length;
    }

    public Object createWrapperType(ObjectMessageContext objCtx, boolean isOutBound) {
        String wrapperType = isOutBound ? getResponseWrapperType()
            : getRequestWrapperType();
        
        Object wrapperObj = null;
        try {
            wrapperObj = Class.forName(wrapperType).newInstance();
        } catch (Exception ex) {
            throw new WebServiceException("Could not create the wrapper element", ex);
        }

        if (isOutBound && getWebResult() != null) {
            setWrappedPart(getWebResult().getLocalPart(), wrapperObj, objCtx.getReturn());
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = method.getParameterTypes().length;

        //Unmarshal parts of mode that should not be ignored and are not part of the SOAP Headers
        Object[] args = objCtx.getMessageObjects();
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                setWrappedPart(param.partName(), wrapperObj, args[idx]);
            }
        }
        return wrapperObj;
    }    
    public void setWrappedPart(String name, Object wrapperType, Object part) {
        try {
            WrapperHelper.setWrappedPart(name, wrapperType, part);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebServiceException("Could not set parts into wrapper element", ex);
        }
    }
    public Object getWrappedPart(Object wrapperType, Class<?> part) {
        try {
            assert wrapperType != null;
            Method elMethods[] = wrapperType.getClass().getMethods();
            for (Method meth : elMethods) {
                if (meth.getParameterTypes().length == 0
                    && meth.getReturnType().equals(part)) {
                    return meth.invoke(wrapperType);
                }
            }
        } catch (Exception ex) {
            throw new WebServiceException("Could not get part out of wrapper element", ex);
        }
        return null;
    }    

    
     
}
