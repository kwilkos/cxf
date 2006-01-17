package org.objectweb.celtix.tools.utils;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.objectweb.celtix.tools.common.toolspec.ToolException;

public final class XMLUtil {

    static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private XMLUtil() {
    }

    public static Transformer newTransformer() throws ToolException {
        try {
            return TRANSFORMER_FACTORY.newTransformer();
        } catch (TransformerConfigurationException tex) {
            throw new ToolException("Unable to create a JAXP transformer", tex);
        }
    }
}
