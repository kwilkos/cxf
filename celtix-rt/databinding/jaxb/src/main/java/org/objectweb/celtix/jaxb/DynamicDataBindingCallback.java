package org.objectweb.celtix.jaxb;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.jaxb.io.SOAPBodyDataReader;
import org.objectweb.celtix.jaxb.io.SOAPBodyDataWriter;
import org.objectweb.celtix.jaxb.io.SOAPMessageDataReader;
import org.objectweb.celtix.jaxb.io.SOAPMessageDataWriter;

public class DynamicDataBindingCallback implements DataBindingCallback {
    
    private static final Logger LOG = LogUtils.getL7dLogger(DynamicDataBindingCallback.class);
    
    protected final Mode mode;
    protected final Class<?>[] clazz;
    protected final JAXBContext context;
    
    public DynamicDataBindingCallback(Class<?> cls, Mode md) {
        mode = md;
        clazz = new Class<?>[] {cls};
        context = null;
    }
    
    public DynamicDataBindingCallback(JAXBContext ctx, Mode md) {
        mode = md;
        context = ctx;
        clazz = new Class<?>[] {Object.class};
    }

    public Mode getMode() {
        return mode;
    }
    
    public JAXBContext getJAXBContext() {
        return context;
    }

    public Class<?>[] getSupportedFormats() {
        return clazz;
    }

    public <T> DataWriter<T> createWriter(Class<T> cls) {
        if (getMode() == Mode.MESSAGE) {
            return new SOAPMessageDataWriter<T>(this);
        } else if ((getMode() == Mode.PAYLOAD) && (cls.isAssignableFrom(SOAPBody.class))) {
            return new SOAPBodyDataWriter<T>(this);          
        }         
        LOG.log(Level.SEVERE, "No DataWriter for class: " + cls.getName());
        return null;
    }

    public <T> DataReader<T> createReader(Class<T> cls) {
        if (getMode() == Mode.MESSAGE) {
            return new SOAPMessageDataReader<T>(this);
        } else if ((getMode() == Mode.PAYLOAD) && (cls.isAssignableFrom(SOAPBody.class))) {
            return new SOAPBodyDataReader<T>(this);          
        }     
        LOG.log(Level.SEVERE, "No DataReader for class: " + cls.getName());
        return null;
    }

    public Style getSOAPStyle() {
        // TODO Auto-generated method stub
        return null;
    }

    public Use getSOAPUse() {
        // TODO Auto-generated method stub
        return null;
    }

    public ParameterStyle getSOAPParameterStyle() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getOperationName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTargetNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSOAPAction() {
        // TODO Auto-generated method stub
        return null;
    }

    public WebResult getWebResult() {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getWebResultQName() {
        // TODO Auto-generated method stub
        return null;
    }

    public WebParam getWebParam(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getParamsLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public WebResult getWebResultAnnotation() {
        // TODO Auto-generated method stub
        return null;
    }

    public WebService getWebService() {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getRequestWrapperQName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRequestWrapperType() {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getResponseWrapperQName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getResponseWrapperType() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isOneWay() {
        // TODO Auto-generated method stub
        return false;
    }

    public void initObjectContext(ObjectMessageContext octx) {
        // TODO Auto-generated method stub
        //REVISIT
    }


}
