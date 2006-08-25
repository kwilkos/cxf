package org.objectweb.celtix.tools.util;

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.InputSource;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ToolException;

public final class StAXUtil {
    private static final Logger LOG = LogUtils.getL7dLogger(StAXUtil.class);
    private static final XMLInputFactory XML_INPUT_FACTORY;
    static {
        XML_INPUT_FACTORY = XMLInputFactory.newInstance();
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
    }

    private StAXUtil() {
    }

    public static void toStartTag(XMLStreamReader r) throws XMLStreamException {
        while (!r.isStartElement() && r.hasNext()) {
            r.next();
        }
    }

    public static XMLStreamReader createFreshXMLStreamReader(InputSource source) {
        try {
            if (source.getCharacterStream() != null) {
                return XML_INPUT_FACTORY.createXMLStreamReader(source.getSystemId(),
                                                             source.getCharacterStream());
            }
            if (source.getByteStream() != null) {
                return XML_INPUT_FACTORY.createXMLStreamReader(source.getSystemId(),
                                                             source.getByteStream());
            }
            return XML_INPUT_FACTORY.createXMLStreamReader(source.getSystemId(),
                                                         new URL(source.getSystemId()).openStream());
        } catch (Exception e) {
            Message msg = new Message("FAIL_TO_CREATE_STAX", LOG);
            throw new ToolException(msg, e);
        }
    }
}
