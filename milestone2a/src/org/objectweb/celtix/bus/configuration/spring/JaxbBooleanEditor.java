package org.objectweb.celtix.bus.configuration.spring;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.bus.configuration.TypeSchema;
import org.objectweb.celtix.bus.configuration.TypeSchemaHelper;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;


public class JaxbBooleanEditor extends CustomBooleanEditor {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JaxbBooleanEditor.class);
    
    public JaxbBooleanEditor() {
        super(false);
    }
    
    public Object getValue() {
        Object o = super.getValue();
        if (o instanceof Element) {
            Element el = (Element)o;
            QName type = new QName(el.getNamespaceURI(), el.getLocalName());
            TypeSchema ts = new TypeSchemaHelper().get(type.getNamespaceURI());
            try {
                return o = ts.unmarshal(type, el);
            } catch (JAXBException ex) {
                Message msg = new Message("JAXB_PROPERTY_EDITOR_EXC", LOG);
                throw new ConfigurationException(msg, ex);
            }
        }

        return o;
    }       
}
