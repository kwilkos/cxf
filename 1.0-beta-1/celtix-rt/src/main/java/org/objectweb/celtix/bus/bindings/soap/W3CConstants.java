package org.objectweb.celtix.bus.bindings.soap;

import javax.xml.namespace.QName;

public final class W3CConstants {
    // XML Namespaces    
    public static final String NP_XMLNS = "xmlns";
    public static final String NU_XMLNS = "http://www.w3.org/2000/xmlns/";

    // XML Schema (CR) datatypes + structures
    public static final String NP_SCHEMA_XSD = "xs";
    public static final String NU_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";

    // XML Schema instance
    public static final String NP_SCHEMA_XSI = "xsi";
    public static final String NU_SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    public static final String A_XSI_TYPE = "type";
    public static final String A_XSI_NIL = "nil";
    
    // XML Schema attribute names
    public static final QName NA_XSI_TYPE = new QName(NP_SCHEMA_XSI, A_XSI_TYPE, NU_SCHEMA_XSI);
    public static final QName NA_XSI_NIL = new QName(NP_SCHEMA_XSI, A_XSI_NIL, NU_SCHEMA_XSI);    
}
