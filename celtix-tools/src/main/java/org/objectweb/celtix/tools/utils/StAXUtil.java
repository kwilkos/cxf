package org.objectweb.celtix.tools.utils;

import java.net.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.InputSource;

import org.objectweb.celtix.tools.common.ToolException;

public final class StAXUtil {

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
            throw new ToolException("stax.cantCreate", e);
        }
    }
}
