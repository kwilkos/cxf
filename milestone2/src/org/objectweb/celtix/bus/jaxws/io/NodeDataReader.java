package org.objectweb.celtix.bus.jaxws.io;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
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
        }
        Node xmlNode = (Node)input;
        
        return JAXBEncoderDecoder.unmarshall(xmlNode, name, cls);
    }
    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        Node xmlNode = (Node)input;
        String wrapperType = isOutBound ? callback.getResponseWrapperType()
            : callback.getRequestWrapperType();
        
        Node childNode = xmlNode.getFirstChild();

        Object retVal = null;
        List<Object> paramList = new ArrayList<Object>();

        QName elName = isOutBound ? callback.getResponseWrapperQName()
            : callback.getRequestWrapperQName();

        Object obj = null;

        try {
            obj = JAXBEncoderDecoder.unmarshall(childNode, elName, Class.forName(wrapperType));
        } catch (ClassNotFoundException e) {
            throw new WebServiceException("Could not unmarshall wrapped type.");
        }

        if (isOutBound && callback.getWebResult() != null) {
            retVal = callback.getWrappedPart(
                             callback.getWebResult().getLocalPart(), 
                             obj, 
                             callback.getMethod().getReturnType());
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getMethod().getParameterTypes().length;
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                paramList.add(
                              callback.getWrappedPart(
                                       param.name(), obj, callback.getMethod().getParameterTypes()[idx]));
            }
        }

        objCtx.setReturn(retVal);
        objCtx.setMessageObjects(paramList.toArray());
    }
}
