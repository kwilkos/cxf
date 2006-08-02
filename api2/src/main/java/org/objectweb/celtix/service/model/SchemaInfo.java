package org.objectweb.celtix.service.model;

import org.w3c.dom.Element;

public final class SchemaInfo extends AbstractPropertiesHolder {
  
    TypeInfo typeInfo;
    String namespaceUri;
    Element element;
    
    public SchemaInfo(TypeInfo typeInfo, String namespaceUri) {
        this.typeInfo = typeInfo;
        this.namespaceUri = namespaceUri;
    }
    
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public String getNamespaceURI() {
        return namespaceUri;
    }

    public void setNamespaceURI(String nsUri) {
        this.namespaceUri = nsUri;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
