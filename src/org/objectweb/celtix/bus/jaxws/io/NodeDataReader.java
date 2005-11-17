package org.objectweb.celtix.bus.jaxws.io;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Node;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.celtix.context.ObjectMessageContext;

public class NodeDataReader<T> implements DataReader<T> {
    final JAXBDataBindingCallback callback;
    
    public NodeDataReader(JAXBDataBindingCallback cb) {
        callback = cb;
    }
    
    public Object read(int idx, T input) {
        return read(null, idx, input);
    }
    
    public Object read(QName name, int idx, T input) {
        Class<?> cls;
        if (idx == -1) {
            cls = callback.getMethod().getReturnType();
        } else {
            cls = callback.getMethod().getParameterTypes()[idx];
            if (cls.isAssignableFrom(Holder.class)) {
                //INOUT and OUT Params are mapped to Holder<T>. 
                Type[] genericParameterTypes = callback.getMethod().getGenericParameterTypes();
                //ParameterizedType represents Holder<?>
                ParameterizedType paramType = (ParameterizedType)genericParameterTypes[idx];
                cls = JAXBEncoderDecoder.getClassFromType(
                                         paramType.getActualTypeArguments()[0]);                
            }
        }
        Node xmlNode = (Node)input;
        
        return JAXBEncoderDecoder.unmarshall(xmlNode, name, cls);
    }
    
    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        Node xmlNode = (Node)input;
        String wrapperType = isOutBound ? callback.getResponseWrapperType()
            : callback.getRequestWrapperType();
        
        Node childNode = xmlNode.getFirstChild();
        Object[] methodArgs = objCtx.getMessageObjects();

        QName elName = isOutBound ? callback.getResponseWrapperQName()
            : callback.getRequestWrapperQName();

        Object obj = null;

        try {
            obj = JAXBEncoderDecoder.unmarshall(childNode, elName, Class.forName(wrapperType));
        } catch (ClassNotFoundException e) {
            throw new WebServiceException("Could not unmarshall wrapped type.");
        }

        if (isOutBound && callback.getWebResult() != null) {
            Object retVal = callback.getWrappedPart(
                             callback.getWebResultQName().getLocalPart(), 
                             obj, 
                             callback.getMethod().getReturnType());
            objCtx.setReturn(retVal);
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getMethod().getParameterTypes().length;
        try {
            for (int idx = 0; idx < noArgs; idx++) {
                WebParam param = callback.getWebParam(idx);
                if ((param.mode() != ignoreParamMode) && !param.header()) {
                    Class<?> cls = callback.getMethod().getParameterTypes()[idx];                
                    if (param.mode() != WebParam.Mode.IN) {
                        //INOUT and OUT Params are mapped to Holder<T>. 
                        Type[] genericParameterTypes = callback.getMethod().getGenericParameterTypes();
                        //ParameterizedType represents Holder<?>
                        ParameterizedType paramType = (ParameterizedType)genericParameterTypes[idx];
                        Class<?> c = 
                            JAXBEncoderDecoder.getClassFromType(paramType.getActualTypeArguments()[0]);
                        Object partValue = callback.getWrappedPart(param.name(), obj, c);
                        //TO avoid type safety warning the Holder 
                        //needs tobe set as below.                        
                        cls.getField("value").set(methodArgs[idx], partValue);
                    } else {
                        methodArgs[idx] = callback.getWrappedPart(param.name(), obj, cls);
                    }
                }
            }
        } catch (IllegalAccessException iae) {
            throw new WebServiceException("Could not unwrap the parts.", iae);
        } catch (NoSuchFieldException nsfe) {
            throw new WebServiceException("Could not unwrap the parts.", nsfe);
        }
    }
}
