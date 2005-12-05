package org.objectweb.celtix.bus.configuration.spring;

import java.beans.PropertyEditor;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.objectweb.celtix.bus.jaxb.JAXBUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;

@SuppressWarnings("deprecation")
public class CeltixXmlBeanFactory extends DefaultListableBeanFactory {

    private static final Logger LOG = LogUtils.getL7dLogger(CeltixXmlBeanFactory.class);
    private static final Class DEFAULT_PARSER_CLASS = CeltixXmlBeanDefinitionParser.class;
    
    private final PropertyEditor editor;

    CeltixXmlBeanFactory(Resource res) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
        reader.setParserClass(DEFAULT_PARSER_CLASS);
        reader.setEntityResolver(new CeltixBeansDtdResolver());
        reader.setValidating(false);
        reader.setNamespaceAware(true);
        reader.loadBeanDefinitions(res);
        
        editor = new JaxbPropertyEditor();
        registerCustomEditor(String.class, editor);
        
        PropertyEditor pe = null;
        
        pe = new JaxbBigIntegerEditor();  
        registerCustomEditor(BigInteger.class, pe);
        
        pe = new JaxbBooleanEditor();
        registerCustomEditor(boolean.class, pe);
        registerCustomEditor(Boolean.class, pe);

        pe = new JaxbNumberEditor(Byte.class);
        registerCustomEditor(byte.class, pe);
        registerCustomEditor(Byte.class, pe);

        pe = new JaxbNumberEditor(Short.class);
        registerCustomEditor(short.class, pe);
        registerCustomEditor(Short.class, pe);

        pe = new JaxbNumberEditor(Integer.class);
        registerCustomEditor(int.class, pe);
        registerCustomEditor(Integer.class, pe);

        pe = new JaxbNumberEditor(Long.class);
        registerCustomEditor(long.class, pe);
        registerCustomEditor(Long.class, pe);

        pe = new JaxbNumberEditor(Float.class);
        registerCustomEditor(float.class, pe);
        registerCustomEditor(Float.class, pe);

        pe = new JaxbNumberEditor(Double.class);
        registerCustomEditor(double.class, pe);
        registerCustomEditor(Double.class, pe);

    }

    void registerCustomEditors(Configuration c) {

        for (ConfigurationItemMetadata definition : c.getModel().getDefinitions()) {
            QName qn = definition.getType();
            String className = BeanGenerator.getClassName(qn, true);

            Class cl = JAXBUtils.holderClass(className);
            if (null != cl) {
                continue;
            }

            try {
                cl = Class.forName(className);
            } catch (ClassCastException ex) {
                throw new ConfigurationException(new Message("COULD_NOT_REGISTER_PROPERTY_EDITOR", LOG,
                                                             className), ex);
            } catch (ClassNotFoundException ex) {
                throw new ConfigurationException(new Message("COULD_NOT_REGISTER_PROPERTY_EDITOR", LOG,
                                                             className), ex);
            }

            if (cl == String.class) {
                continue;
            }

            if (null == getCustomEditors().get(cl)) {
                registerCustomEditor(cl, editor);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Registered JaxbPropertyEditor for class: " + className);
                }
            }
        }
    }

}
