package org.objectweb.celtix.servicemodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import org.objectweb.celtix.jaxb.JAXBUtils;

//REVISIT - move to JAXWS package
/**
 * Builder to build a Service object from a JAX-WS Annotated Class file
 */
public final class JAXWSClassServiceBuilder {
    public static final String JAXWS_CLASS = JAXWSClassServiceBuilder.class.getName()
                                             + ".JAXWS_CLASS";
    public static final String JAXWS_METHOD = JAXWSClassServiceBuilder.class.getName()
                                             + ".JAXWS_METHOD";    
    public static final String JAXWS_WRAPPER_CLASS_NAME = JAXWSClassServiceBuilder.class.getName()
                                             + ".JAXWS_WRAPPER_CLASS_NAME";
    public static final String JAXWS_FAULT_CLASS = JAXWSClassServiceBuilder.class.getName()
                                             + ".JAXWS_FAULT_CLASS";
    
    private JAXWSClassServiceBuilder() {
        //utility class - never contructed
    }
    
    
    public static Service buildService(Class<?> cls) {
        Service service = new Service();
        service.setProperty(JAXWS_CLASS, cls);
        
        WebService webServiceAnnotation = cls.getAnnotation(WebService.class);
        if (webServiceAnnotation != null) {
            service.setTargetNamespace(webServiceAnnotation.targetNamespace());
        }
        
        for (Method method : cls.getMethods()) {
            WebMethod webMethod = method.getAnnotation(WebMethod.class);
            if (webMethod != null
                && !JAXBUtils.isAsync(method)) {
                //don't do the Async methods as the information will come fine from the sync versions
                addMethod(service, method);
            }
        }
        
        return service;
    }
    
    private static void addMethod(Service service, Method method) {
        WebMethod webMethod = method.getAnnotation(WebMethod.class);
        OperationInfo op = service.addOperation(webMethod.operationName());
        op.setProperty(JAXWS_METHOD, method);
        op.setSOAPAction(webMethod.action());
        
        SOAPBinding soapBindAnnotation = method.getAnnotation(SOAPBinding.class);
        if (soapBindAnnotation != null) {
            soapBindAnnotation = method.getDeclaringClass().getAnnotation(SOAPBinding.class);
        }

        MessageInfo min = op.createMessage(new QName(service.getTargetNamespace(), op.getName()));
        op.setInput(min);
        
        MessageInfo mout = op.createMessage(new QName(service.getTargetNamespace(),
                                                      op.getName() + "Response"));
        if (method.getAnnotation(Oneway.class) == null) {
            op.setOutput(mout);
        }
        
        if (soapBindAnnotation != null) {
            op.setRPC(soapBindAnnotation.style() == SOAPBinding.Style.RPC);
            min.setLiteral(soapBindAnnotation.use() == SOAPBinding.Use.LITERAL);
            mout.setLiteral(soapBindAnnotation.use() == SOAPBinding.Use.LITERAL);
            //REVISIT - Param style
        }
        
        WebResult webResult = method.getAnnotation(WebResult.class);
        if (webResult != null) {
            QName qn;
            if (!op.isRPC()) {
                if ("".equals(webResult.name())) {
                    qn = new QName(webResult.targetNamespace(),
                            "return");
                } else {
                    qn = new QName(webResult.targetNamespace(),
                                 webResult.name());
                }
            } else {
                qn = new QName("", webResult.partName());
            }
            MessagePartInfo minfo = mout.addMessagePart(qn);
            minfo.setHeader(webResult.header());
        }
        for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
            for (Annotation anot : paramAnnotations) {
                if (anot instanceof WebParam) {
                    WebParam wb = (WebParam)anot;
                    QName qn = new QName(wb.targetNamespace(), wb.name());
                    if (wb.mode() != WebParam.Mode.OUT) {
                        MessagePartInfo mpi = min.addMessagePart(qn);
                        mpi.setHeader(wb.header());
                        mpi.setPartName(wb.partName());
                        mpi.setInOut(wb.mode() == WebParam.Mode.INOUT);
                    }
                    if (wb.mode() != WebParam.Mode.IN) {
                        MessagePartInfo mpi = mout.addMessagePart(qn);
                        mpi.setHeader(wb.header());
                        mpi.setPartName(wb.partName());
                        mpi.setInOut(wb.mode() == WebParam.Mode.INOUT);
                    }
                }
            }
        } 
        
        //Get the RequestWrapper
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        if (reqWrapper != null) {
            min.setWrapperQName(new QName(reqWrapper.targetNamespace(), reqWrapper.localName()));
            min.setProperty(JAXWS_WRAPPER_CLASS_NAME, reqWrapper.className());
        }
        //Get the RequestWrapper
        ResponseWrapper respWrapper = method.getAnnotation(ResponseWrapper.class);
        if (respWrapper != null) {
            mout.setWrapperQName(new QName(respWrapper.targetNamespace(), respWrapper.localName()));
            mout.setProperty(JAXWS_WRAPPER_CLASS_NAME, respWrapper.className());
        }  
        
        for (Class<?> exc : method.getExceptionTypes()) {
            WebFault webFault = exc.getAnnotation(WebFault.class);
            if (webFault != null) {
                FaultInfo fi  = op.addFault(webFault.name(),
                                            new QName(webFault.targetNamespace(), webFault.name()));
                fi.setProperty(JAXWS_FAULT_CLASS, exc);
            }
        }
        
    }
}
