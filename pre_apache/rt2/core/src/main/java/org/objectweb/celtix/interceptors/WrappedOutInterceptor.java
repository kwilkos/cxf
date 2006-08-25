package org.objectweb.celtix.interceptors;

import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;

public class WrappedOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WrappedOutInterceptor.class);

    public WrappedOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
        addBefore(BareOutInterceptor.class.getName());
    }

    public void handleMessage(Message message) {
        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        if (bop.isUnwrapped()) {
            XMLStreamWriter xmlWriter = getXMLStreamWriter(message);

            MessageInfo messageInfo = message.get(MessageInfo.class);
            QName name = messageInfo.getName();

            try {
                xmlWriter.writeStartElement(name.getLocalPart(), name.getNamespaceURI());
                message.getInterceptorChain().doIntercept(message);
                xmlWriter.writeEndElement();
            } catch (XMLStreamException e) {
                throw new Fault(new org.objectweb.celtix.common.i18n.Message("STAX_READ_EXC", BUNDLE, e));
            }
        }
    }

    protected XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }
}
