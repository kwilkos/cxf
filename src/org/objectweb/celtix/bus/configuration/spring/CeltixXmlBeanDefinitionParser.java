package org.objectweb.celtix.bus.configuration.spring;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser;

public class CeltixXmlBeanDefinitionParser extends DefaultXmlBeanDefinitionParser {

    protected Object parsePropertySubElement(Element valueElement, String beanName) {

        if (valueElement.getTagName().equals(VALUE_ELEMENT)) {
            for (Node nd = valueElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                if (Node.ELEMENT_NODE == nd.getNodeType()) {
                    return nd;
                }
            }
        }
        return super.parsePropertySubElement(valueElement, beanName);
    }
}
