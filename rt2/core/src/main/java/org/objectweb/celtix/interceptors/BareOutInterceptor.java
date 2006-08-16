package org.objectweb.celtix.interceptors;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;

public class BareOutInterceptor extends AbstractOutDatabindingInterceptor {

    public BareOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {
        XMLStreamWriter xmlWriter = getXMLStreamWriter(message);

        Exchange exchange = message.getExchange();
        BindingOperationInfo operation = (BindingOperationInfo)exchange.get(BindingOperationInfo.class
            .getName());
        DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message);

        int countParts = 0;
        List<MessagePartInfo> parts = null;
        if (isOutboundMessage(message)) {
            parts = operation.getOutput().getMessageInfo().getMessageParts();
        } else {
            parts = operation.getInput().getMessageInfo().getMessageParts();
        }
        countParts = parts.size();

        if (countParts > 0) {
            List<?> objs = (List<?>)message.getContent(Object.class);
            Object[] args = objs.toArray();
            Object[] els = parts.toArray();

            if (args.length != els.length) {
                message.setContent(Exception.class,
                                   new RuntimeException("The number of arguments is not equal!"));
            }

            for (int idx = 0; idx < countParts; idx++) {
                Object arg = args[idx];
                MessagePartInfo part = (MessagePartInfo)els[idx];
                QName elName = ServiceModelUtil.getPartName(part);
                dataWriter.write(arg, elName, xmlWriter);
            }
        }
    }
}
