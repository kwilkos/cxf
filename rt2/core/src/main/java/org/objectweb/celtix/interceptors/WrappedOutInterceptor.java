package org.objectweb.celtix.interceptors;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;

public class WrappedOutInterceptor extends AbstractOutDatabindingInterceptor {
    public WrappedOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {
        XMLStreamWriter xmlWriter = getXMLStreamWriter(message);
        DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message);

        // TODO: Accomodate non wrapper case and actually write the wrapped element
        
        List<?> objs = message.getContent(List.class);

        if (objs != null && objs.size() > 0) {
            dataWriter.write(objs.get(0), xmlWriter);
        }
    }

}
