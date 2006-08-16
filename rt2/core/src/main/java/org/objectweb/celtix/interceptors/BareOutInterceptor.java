package org.objectweb.celtix.interceptors;

import java.io.*;
import java.util.*;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.databinding.DataWriterFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;

public class BareOutInterceptor extends AbstractPhaseInterceptor<Message> {

    public BareOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {
        try {
            String opName = (String) message.get(Message.INVOCATION_OPERATION);
            XMLStreamWriter xmlWriter = getXMLStreamWriter(message);

            BindingOperationInfo operation = ServiceModelUtil.getOperation(message, opName);
            DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message, operation.getOperationInfo());
            
            int countParts = 0;
            List<MessagePartInfo> parts = null;
            if (isOutboundMessage(message)) {
                parts = operation.getOutput().getMessageInfo().getMessageParts();
            } else {
                parts = operation.getInput().getMessageInfo().getMessageParts();
            }
            countParts = parts.size();

            if (countParts > 0) {
                List<?> objs = (List<?>) message.get(Message.INVOCATION_OBJECTS);
                Object[] args = objs.toArray();
                Object[] els  = parts.toArray();

                if (args.length != els.length) {
                    message.setContent(Exception.class,
                                       new RuntimeException("The number of arguments is not equal!"));
                }
                
                for (int idx = 0; idx < countParts; idx++) {
                    Object arg = args[idx];
                    MessagePartInfo  part = (MessagePartInfo) els[idx];
                    QName elName = ServiceModelUtil.getPartName(part);
                    dataWriter.write(arg, elName, xmlWriter);
                }
            }
            // Finishing the writing.
            xmlWriter.flush();
            xmlWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            message.setContent(Exception.class, e);
        }
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

    protected boolean isOutboundMessage(Message message) {
        return message.containsKey(Message.INBOUND_MESSAGE);
    }

    private XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }
}
