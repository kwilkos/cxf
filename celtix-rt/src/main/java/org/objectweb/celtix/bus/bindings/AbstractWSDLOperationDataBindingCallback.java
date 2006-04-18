package org.objectweb.celtix.bus.bindings;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.namespace.QName;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;

public abstract class AbstractWSDLOperationDataBindingCallback implements DataBindingCallback {
    WSDLOperationInfo operation;
    
    public AbstractWSDLOperationDataBindingCallback(WSDLOperationInfo op) {
        operation = op;
    }
    
    
    public abstract Mode getMode();
    public abstract Class<?>[] getSupportedFormats();
    public abstract <T> DataWriter<T> createWriter(Class<T> cls);
    public abstract <T> DataReader<T> createReader(Class<T> cls);
    
    
    public Style getSOAPStyle() {
        return operation.getSOAPStyle();
    }

    public Use getSOAPUse() {
        return operation.getSOAPUse();
    }

    public ParameterStyle getSOAPParameterStyle() {
        return operation.getSOAPParameterStyle();
    }

    public String getOperationName() {
        return operation.getOperationName();
    }

    public String getTargetNamespace() {
        return operation.getTargetNamespace();
    }

    public String getSOAPAction() {
        return operation.getSOAPAction();
    }

    public WebResult getWebResult() {
        return operation.getWebResult();
    }

    public QName getWebResultQName() {
        return operation.getWebResultQName();
    }

    public WebParam getWebParam(int index) {
        return operation.getWebParam(index);
    }

    public int getParamsLength() {
        return operation.getParamsLength();
    }

    public QName getRequestWrapperQName() {
        return operation.getRequestWrapperQName();
    }

    public QName getResponseWrapperQName() {
        return operation.getResponseWrapperQName();
    }

}
