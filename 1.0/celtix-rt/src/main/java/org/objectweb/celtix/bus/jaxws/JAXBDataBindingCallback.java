package org.objectweb.celtix.bus.jaxws;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPFault;
import javax.xml.validation.Schema;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Node;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bindings.ServerDataBindingCallback;
import org.objectweb.celtix.bus.bindings.soap.SOAPConstants;
import org.objectweb.celtix.bus.bindings.xml.XMLFault;
import org.objectweb.celtix.bus.jaxws.io.DetailDataWriter;
import org.objectweb.celtix.bus.jaxws.io.NodeDataReader;
import org.objectweb.celtix.bus.jaxws.io.NodeDataWriter;
import org.objectweb.celtix.bus.jaxws.io.SOAPFaultDataReader;
import org.objectweb.celtix.bus.jaxws.io.XMLFaultReader;
import org.objectweb.celtix.bus.jaxws.io.XMLFaultWriter;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.jaxb.JAXBUtils;
import org.objectweb.celtix.jaxb.WrapperHelper;


public class JAXBDataBindingCallback implements ServerDataBindingCallback {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JAXBDataBindingCallback.class);
    
    private SOAPBinding soapBindAnnotation;
    private WebMethod webMethodAnnotation;
    private WebResult webResultAnnotation;
    private Annotation[][] paramAnnotations;
    private RequestWrapper reqWrapper;
    private ResponseWrapper respWrapper;
    private final Method method;
    private Method syncMethod;
    private final Mode mode;
    private WebService webServiceAnnotation;
    private final JAXBContext context;
    private final Schema schema;
    private final EndpointImpl endpoint;
    private final Object impl;
    
    public JAXBDataBindingCallback(Method m, Mode md, JAXBContext ctx) {
        this(m, md, ctx, null);
    }
    public JAXBDataBindingCallback(Method m, Mode md, JAXBContext ctx, Schema s) {
        this(m, md, ctx, s, null);
    }
    
    public JAXBDataBindingCallback(Method m, Mode md, JAXBContext ctx, Schema s, EndpointImpl ep) {
        method = m;
        mode = md;
        context = ctx;
        schema = s;
        endpoint = ep;
        impl = null;
        init();
    }
    public JAXBDataBindingCallback(Method m, Mode md, JAXBContext ctx, Schema s, Object obj) {
        method = m;
        mode = md;
        context = ctx;
        schema = s;
        endpoint = null;
        impl = obj;
        init();
    }

    public JAXBContext getJAXBContext() {
        return context;
    }
    
    public Schema getSchema() {
        return schema;
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
        } else if (cls == XMLFault.class) {
            return new XMLFaultWriter<T>(this);
        }
        // TODO Auto-generated method stub
        return null;
    }

    public <T> DataReader<T> createReader(Class<T> cls) {
        if (cls == Node.class) {
            return new NodeDataReader<T>(this);
        } else if (cls == SOAPFault.class) { 
            return new SOAPFaultDataReader<T>(this);
        } else if (cls == XMLFault.class) {
            return new XMLFaultReader<T>(this);
        }
        // TODO Auto-generated method stub
        return null;
    }

    private void init() {
        if (method != null) {
            //Get WebService Annotation.
            webServiceAnnotation = method.getDeclaringClass().getAnnotation(WebService.class);
            //Get SOAP Style, Use,
            soapBindAnnotation = method.getAnnotation(SOAPBinding.class);
            if (soapBindAnnotation == null) {
                soapBindAnnotation = method.getDeclaringClass().getAnnotation(SOAPBinding.class);            
            }
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
            
            if (JAXBUtils.isAsync(method)) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes != null && paramTypes.length > 0 
                    && AsyncHandler.class.isAssignableFrom(paramTypes[paramTypes.length - 1])) {
                    Class[] effectiveParamTypes = new Class[paramTypes.length - 1];
                    System.arraycopy(paramTypes, 0, effectiveParamTypes, 0, paramTypes.length - 1);
                    paramTypes = effectiveParamTypes;
                }
                String syncMethodName = method.getName().substring(0, method.getName().lastIndexOf("Async"));
                try {
                    syncMethod = method.getDeclaringClass().getMethod(syncMethodName, paramTypes);
                    webResultAnnotation = syncMethod.getAnnotation(WebResult.class);
                    assert null != webResultAnnotation;
                } catch (NoSuchMethodException ex) {
                    LOG.severe("Could not find method " + syncMethodName 
                               + " in class declaring method " + method.getName()); 
                }
                
                if (null == webResultAnnotation) {
                    webResultAnnotation = syncMethod.getAnnotation(WebResult.class);
                }
            }
        }
    }
    
    public boolean isOneWay() {
        if (method != null) {
            return method.getAnnotation(Oneway.class) != null;
        }
        return false;
    }    


    public SOAPBinding.Style getSOAPStyle() {
        if (null != soapBindAnnotation) {
            return soapBindAnnotation.style();
        }
        if (endpoint != null) {
            return endpoint.getStyle();            
        }
        return Style.DOCUMENT;
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
        if (webServiceAnnotation == null) {
            return "";
        }
        return webServiceAnnotation.targetNamespace();
    }

    public String getOperationName() {
        if (null != webMethodAnnotation &&  !"".equals(webMethodAnnotation.operationName())) {
            return webMethodAnnotation.operationName();
        }
        if (getMethod() == null) {
            return "";
        }
        return getMethod().getName();
    }

    public String getSOAPAction() {
        if (null != webMethodAnnotation) {
            return webMethodAnnotation.action();
        }
        return "";
    }

    public WebResult getWebResult() {
        return webResultAnnotation;
    }

    public QName getWebResultQName() {
        if (null != webResultAnnotation) {
            if (getSOAPStyle() == Style.DOCUMENT) {
                if ("".equals(webResultAnnotation.name())) {
                    return new QName(webResultAnnotation.targetNamespace(),
                            "return");
                }
                return new QName(webResultAnnotation.targetNamespace(),
                                 webResultAnnotation.name());
            } else {
                return new QName("", webResultAnnotation.partName());
            }
        }
        return SOAPConstants.EMPTY_QNAME;
    }
    
    public WebParam getWebParam(int index) {
        if (null != paramAnnotations && index < paramAnnotations.length) {
            for (Annotation annotation : paramAnnotations[index]) {
                if (WebParam.class.equals(annotation.annotationType())) {
                    return (WebParam)annotation;
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
    
    public Method getSyncMethod() {
        return syncMethod;
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
        return getMethod() != null 
               ? getMethod().getParameterTypes().length
               : 0;
    }

    public Object createWrapperType(ObjectMessageContext objCtx, boolean isOutBound) {
        String wrapperType = isOutBound ? getResponseWrapperType()
            : getRequestWrapperType();
        
        Object wrapperObj = null;
        try {
            
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) { 
                loader = getClass().getClassLoader();
            } 

            if (!"".equals(wrapperType)) {
                wrapperObj = Class.forName(wrapperType, true, loader).newInstance();
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new WebServiceException("Could not create the wrapper element", ex);
        }

        if (isOutBound && getWebResult() != null) {
            setWrappedPart(getWebResultQName().getLocalPart(), wrapperObj, objCtx.getReturn());
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        
        int noArgs = getMethod().getParameterTypes().length;
        
        //Unmarshal parts of mode that should not be ignored and are not part of the SOAP Headers
        Object[] args = objCtx.getMessageObjects();
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                Object wrappedObj = args[idx];
                //Unwrap Holder for inout,out parts.                
                if (param.mode() != WebParam.Mode.IN) {
                    wrappedObj = ((Holder)wrappedObj).value;    
                }        
                if (param.name().equals("asyncHandler") && idx == (noArgs - 1)) {
                    break;
                }
                
                setWrappedPart(param.name(), wrapperObj, wrappedObj);
            }
        }
    
        return wrapperObj;
    }    
    public void setWrappedPart(String name, Object wrapperType, Object part) {
        try {
            WrapperHelper.setWrappedPart(name, wrapperType, part);
        } catch (Exception ex) {
            throw new WebServiceException("Could not set parts into wrapper element", ex);
        }
    }
    public Object getWrappedPart(String name, Object wrapperType, Class<?> part) {
        Object obj = null;
        try {
            assert wrapperType != null;            
            obj = WrapperHelper.getWrappedPart(name, wrapperType, part);
            assert obj != null;
        } catch (Exception ex) {
            throw new WebServiceException("Could not get part out of wrapper element", ex);
        }
        return obj;
    }

    public void initObjectContext(ObjectMessageContext octx) {
        if (method != null) {
            octx.put(ObjectMessageContext.METHOD_OBJ, method);
            try {
                int idx = 0;
                Object[] methodArgs = new Object[method.getParameterTypes().length];
                for (Class<?> cls : method.getParameterTypes()) {
                    if (cls.isAssignableFrom(Holder.class)) {
                        methodArgs[idx] = cls.newInstance();
                    }
                    idx++;
                }
                octx.setMessageObjects(methodArgs);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "INIT_OBJ_CONTEXT_FAILED");
                throw new WebServiceException(ex);
            }
        }
    }

    public void invoke(ObjectMessageContext octx) throws InvocationTargetException {
        Object o = impl;
        try {
            if (o == null) {
                o = endpoint.getImplementor();
            }
            Object ret = method.invoke(o, octx.getMessageObjects());

            octx.setReturn(ret);
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (impl == null) {
                endpoint.releaseImplementor(o);
            }
        }
    }


    
     
}
