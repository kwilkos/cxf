package org.objectweb.celtix.tools.jaxws;

import org.w3c.dom.Element;

public class BindingsNode {

    private String xpathExpression;
    private Class parentType;
    private String nodeName;
    private Element element;

    public Element getElement() {
        return this.element;
    }

    public void setElement(Element elem) {
        this.element = elem;
    }
    
    public Class getParentType() {
        return this.parentType;
    }

    public void setParentType(Class clz) {
        parentType = clz;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nn) {
        this.nodeName = nn;
    }

    public String getXPathExpression() {
        return this.xpathExpression;
    }

    public void setXPathExpression(String xe) {
        this.xpathExpression = xe;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nBindingNode");
        sb.append("[");
        sb.append(xpathExpression);
        sb.append("]");
        sb.append("[");
        sb.append(parentType);
        sb.append("]");
        sb.append("[");
        sb.append(nodeName);
        sb.append("]\n");
        return sb.toString();
    }
}
