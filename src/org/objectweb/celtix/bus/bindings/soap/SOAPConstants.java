package org.objectweb.celtix.bus.bindings.soap;

import javax.xml.namespace.QName;

public final class SOAPConstants {
    public static final String SOAP_ENV_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final QName  EMPTY_QNAME = new QName("", "");    
    public static final QName  SOAP_ENV_ENCSTYLE = 
            new QName("http://schemas.xmlsoap.org/soap/envelope/", "encodingStyle");
    public static final QName  SOAP_ENV = 
            new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
    public static final QName  SOAP_HEADER = 
            new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header");
    public static final QName  SOAP_BODY = 
            new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body");
    public static final QName  SOAP_FAULT = 
            new QName("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
}
