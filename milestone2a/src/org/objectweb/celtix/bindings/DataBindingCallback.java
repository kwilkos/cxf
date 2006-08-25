package org.objectweb.celtix.bindings;


import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * Callback used during IO for the bindings to figure out how to properly construct the messages. 
 */
public interface DataBindingCallback {
    
    public enum Mode { 
        MESSAGE(Service.Mode.MESSAGE),
        PAYLOAD(Service.Mode.PAYLOAD),
        PARTS(null);
    
    /* this indentation is WRONG.   There is a bug in checkstyle already reported */
    Service.Mode jaxwsMode;
    Mode(Service.Mode m) {
        jaxwsMode = m;
    }
    public static Mode fromServiceMode(Service.Mode m) {
        if (m == Service.Mode.PAYLOAD) {
            return PAYLOAD;
        }
        return MESSAGE;
    }
    public Service.Mode getServiceMode() {
        return jaxwsMode;
    }
    };    

    Mode getMode();

    Class<?>[] getSupportedFormats();
    
    <T> DataWriter<T> createWriter(Class<T> cls);
    <T> DataReader<T> createReader(Class<T> cls);
       
    
    SOAPBinding.Style getSOAPStyle();
    SOAPBinding.Use getSOAPUse();
    SOAPBinding.ParameterStyle getSOAPParameterStyle();
    
    String getOperationName();
    String getTargetNamespace();
    String getSOAPAction();
    QName getWebResult();
    WebParam getWebParam(int index);
    int getParamsLength();
    
    WebResult getWebResultAnnotation();
    WebService getWebService();    
    
    QName getRequestWrapperQName();
    String getRequestWrapperType();
    QName getResponseWrapperQName();
    String getResponseWrapperType();
}
