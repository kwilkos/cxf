package org.apache.cxf.interceptor;

import java.io.InputStream;
import java.util.ResourceBundle;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Creates an XMLStreamReader from the InputStream on the Message.
 */
public class StaxInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(StaxInInterceptor.class);
    private XMLInputFactory xif;
    
    public StaxInInterceptor() {
        super();
        setPhase(Phase.POST_STREAM);
    }

    public void handleMessage(Message message) {
        InputStream is = message.getContent(InputStream.class);

        assert is != null;

        // TODO: where does encoding constant go?
        String encoding = (String)message.get("Encoding");
        XMLStreamReader reader;
        try {
            reader = getXMLInputFactory().createXMLStreamReader(is, encoding);
        } catch (XMLStreamException e) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("STREAM_CREATE_EXC", BUNDLE), e);
        }

        message.setContent(XMLStreamReader.class, reader);
    }

    protected XMLInputFactory getXMLInputFactory() {
        if (xif == null) {
            return XMLInputFactory.newInstance();
        }

        return xif;
    }
}
