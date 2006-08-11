package org.objectweb.celtix.interceptors;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;

public class BareInInterceptor extends AbstractPhaseInterceptor<Message> {

    protected boolean isOperationResolved(Message message) {
        return message.get(Message.INVOCATION_OPERATION) != null;
    }

    public void handleMessage(Message message) {
        String opName = (String) message.get(Message.INVOCATION_OPERATION);

        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);
        BindingOperationInfo operation = ServiceModelUtil.getOperation(message, opName);

        DataReader<XMLStreamReader> dr = getDataReader(message, operation.getOperationInfo());
        
        MessageInfo msg;

        if (isInboundMessage(message)) {
            msg = operation.getInput().getMessageInfo();
        } else {
            msg = operation.getOutput().getMessageInfo();
        }

        List<Object> parameters = new ArrayList<Object>();

        StaxUtils.nextEvent(xmlReader);
        while (StaxUtils.toNextElement(xmlReader)) {
            int idx = parameters.size();
            MessagePartInfo p = msg.getMessageParts().get(idx);
            if (p == null) {
                message.setContent(Exception.class,
                                   new RuntimeException("Parameter "
                                                        + xmlReader.getName()
                                                        + " does not exist!"));
            }
            QName name = xmlReader.getName();
            QName elName = ServiceModelUtil.getPartName(p);
            
            if (!elName.equals(name)) {
                message.setContent(Exception.class,
                                   new RuntimeException("Parameter " + name + " does not exist!"));
            }
            parameters.add(dr.read(elName,
                                   xmlReader));
        }
        
        message.put(Message.INVOCATION_OBJECTS, parameters);
    }

    protected DataReader<XMLStreamReader> getDataReader(Message message, OperationInfo oi) {
        String key = (String) message.getExchange().get(Message.DATAREADER_FACTORY_KEY);
        DataReaderFactory factory = (DataReaderFactory) oi.getProperty(key);

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = factory.createReader(XMLStreamReader.class);
                break;
            }
        }
        if (dataReader == null) {
            message.setContent(Exception.class,
                               new RuntimeException("Could not figure out how to unmarshal data"));
        }        
        return dataReader;
    }
    
    protected boolean isInboundMessage(Message message) {
        return message.containsKey(Message.INBOUND_MESSAGE);
    }

    private DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(message.getContent(InputStream.class));
        return new DepthXMLStreamReader(xr);
    }
}
