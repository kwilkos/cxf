package org.objectweb.celtix.interceptors;

import java.io.OutputStream;
import java.util.ResourceBundle;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;

/**
 * Creates an XMLStreamReader from the InputStream on the Message.
 */
public class StaxOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(StaxOutInterceptor.class);
    private XMLOutputFactory xof;
    
    public StaxOutInterceptor() {
        super();
        setPhase(Phase.PRE_PROTOCOL);
    }

    public void handleMessage(Message message) {
        OutputStream os = message.getContent(OutputStream.class);

        assert os != null;

        // TODO: where does encoding constant go?
        String encoding = (String)message.get("Encoding");
        XMLStreamWriter writer;
        try {
            writer = getXMLOutputFactory().createXMLStreamWriter(os, encoding);
        } catch (XMLStreamException e) {
            throw new Fault(new org.objectweb.celtix.common.i18n.Message("STREAM_CREATE_EXC", BUNDLE), e);
        }

        message.setContent(XMLStreamWriter.class, writer);
    }

    protected XMLOutputFactory getXMLOutputFactory() {
        if (xof == null) {
            return XMLOutputFactory.newInstance();
        }

        return xof;
    }
}
