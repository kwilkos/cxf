package org.apache.cxf.interceptor;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceModelUtil;

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
        if (!isRequestor(message)) {
            parts = operation.getOutput().getMessageInfo().getMessageParts();
        } else {
            parts = operation.getInput().getMessageInfo().getMessageParts();
        }
        countParts = parts.size();

        if (countParts > 0) {
            List<?> objs = message.getContent(List.class);
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
