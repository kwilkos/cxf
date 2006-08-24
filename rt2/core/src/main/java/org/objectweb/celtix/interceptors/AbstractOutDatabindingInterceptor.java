package org.objectweb.celtix.interceptors;

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.databinding.DataWriterFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.ServiceModelUtil;

public abstract class AbstractOutDatabindingInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils
        .getBundle(AbstractOutDatabindingInterceptor.class);

    protected boolean isRequestor(Message message) {
        return Boolean.TRUE.equals(message.containsKey(Message.REQUESTOR_ROLE));
    }
    
    protected DataWriter<XMLStreamWriter> getDataWriter(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataWriterFactory factory = service.getDataWriterFactory();

        DataWriter<XMLStreamWriter> dataWriter = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamWriter.class) {
                dataWriter = factory.createWriter(XMLStreamWriter.class);
                break;
            }
        }

        if (dataWriter == null) {
            throw new Fault(new org.objectweb.celtix.common.i18n.Message("NO_DATAWRITER", BUNDLE, service
                .getName()));
        }

        return dataWriter;
    }

    protected XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }
}
