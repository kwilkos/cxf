package org.objectweb.celtix.datamodel.xml;

import javax.xml.namespace.QName;

public final class XMLConstants {
    static final String NS_XML_FORMAT = "http://celtix.objectweb.org/bindings/xmlformat";

    static final QName XML_FAULT_ROOT = new QName(NS_XML_FORMAT, "XMLFault");
    static final QName XML_FAULT_CODE = new QName(NS_XML_FORMAT, "faultcode");
    static final QName XML_FAULT_STRING = new QName(NS_XML_FORMAT, "faultstring");
    static final QName XML_FAULT_DETAIL = new QName(NS_XML_FORMAT, "detail");

    static final QName XML_FAULT_CODE_SERVER = new QName(NS_XML_FORMAT, "SERVER");
    static final QName XML_FAULT_CODE_CLIENT = new QName(NS_XML_FORMAT, "CLIENT");
}
