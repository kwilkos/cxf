package org.objectweb.celtix.interceptors;

import java.util.*;

import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.databinding.DataWriterFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;


public class WrappedOutInterceptor extends AbstractPhaseInterceptor<Message> {
    
    public void handleMessage(Message message) {
        try {
            String opName = (String) message.get(Message.INVOCATION_OPERATION);
            XMLStreamWriter xmlWriter = getXMLStreamWriter(message);
            BindingOperationInfo operation = ServiceModelUtil.getOperation(message, opName);
            DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message, operation.getOperationInfo());

            List<?> objs = (List<?>) message.get(message.INVOCATION_OBJECTS);
            
            if (objs != null && objs.size() > 0) {
                dataWriter.write(objs.get(0),
                                 xmlWriter);
            }
            
            xmlWriter.flush();
            xmlWriter.close();
        } catch (Exception e) {
            message.setContent(Exception.class, e);
        }
    }

    protected boolean isOutboundMessage(Message message) {
        return message.containsKey(Message.INBOUND_MESSAGE);
    }

    protected DataWriter<XMLStreamWriter> getDataWriter(Message message, OperationInfo oi) {
        String key = (String) message.getExchange().get(Message.DATAWRITER_FACTORY_KEY);
        DataWriterFactory factory = (DataWriterFactory) oi.getProperty(key);

        DataWriter<XMLStreamWriter> dataWriter = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamWriter.class) {
                dataWriter = factory.createWriter(XMLStreamWriter.class);
                break;
            }
        }
        if (dataWriter == null) {
            message.setContent(Exception.class,
                               new RuntimeException("Could not figure out how to marshal data"));
        }        
        return dataWriter;
    }
    
    private XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }
}
