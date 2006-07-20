package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.soap2.AbstractSoapInterceptor;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.bindings.soap2.SoapVersion;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxStreamFilter;
import org.objectweb.celtix.staxutils.StaxUtils;

public class BareInterceptor extends AbstractSoapInterceptor {

    private static final String INBOUND_MESSAGE = "message.inbound";
    
    private SoapMessage soapMessage;
    private DepthXMLStreamReader xmlReader;

    private void init(SoapMessage message) {
        this.soapMessage = message;
        this.xmlReader = getXMLStreamReader();
    }

    public void handleMessage(SoapMessage message) {
        init(message);

        if (!isInboundMessage() && getCallback().getWebResult() != null) {
            Object retVal = getDataReader().read(getCallback().getWebResultQName(),
                                                 -1,
                                                 this.xmlReader);
            message.put("RETURN", retVal);
        }

        
        List<Object> parameters = new ArrayList<Object>();
        WebParam.Mode ignoreParamMode = isInboundMessage() ? WebParam.Mode.OUT : WebParam.Mode.IN;
        int noArgs = getCallback().getParamsLength();

        StaxUtils.nextEvent(this.xmlReader);

        for (int idx = 0; idx < noArgs; idx++) {
            StaxUtils.toNextElement(this.xmlReader);
            WebParam param = getCallback().getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                QName elName = new QName(param.targetNamespace(), param.name());

                Object obj = getDataReader().read(elName, idx, this.xmlReader);

                parameters.add(obj);
            }
        }
        
        message.put("PARAMETERS", parameters);
        
        message.getInterceptorChain().doIntercept(message);
    }

    protected DataReader<XMLStreamReader> getDataReader() {
        DataBindingCallback callback = getCallback();

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : callback.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = callback.createReader(XMLStreamReader.class);
                break;
            }
        }

        if (dataReader == null) {
            throw new RuntimeException("Could not figure out how to unmarshal data");
        }
        return dataReader;
    }

    protected boolean isInboundMessage() {
        return this.soapMessage.containsKey(INBOUND_MESSAGE);
    }

    private DataBindingCallback getCallback() {
        return (DataBindingCallback) this.soapMessage.get("JAXB_CALLBACK");
    }

    private DepthXMLStreamReader getXMLStreamReader() {
        SoapVersion version = this.soapMessage.getVersion();
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(this.soapMessage.getSource(InputStream.class));
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{version.getEnvelope(),
                                                                   version.getBody()});
        xr = StaxUtils.createFilteredReader(xr, filter);
        return new DepthXMLStreamReader(xr);
    }
}
