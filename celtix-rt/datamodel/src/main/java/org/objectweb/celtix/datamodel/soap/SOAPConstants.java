package org.objectweb.celtix.datamodel.soap;

import javax.xml.namespace.QName;

public final class SOAPConstants {
    public static final String SOAP_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
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
    //SOAP1.1 FaultCodes
    public static final QName  FAULTCODE_VERSIONMISMATCH = 
            new QName(SOAP_ENV_URI, "VersionMismatch");
    public static final QName  FAULTCODE_MUSTUNDERSTAND = 
            new QName(SOAP_ENV_URI, "MustUnderstand");
    public static final QName  FAULTCODE_CLIENT = 
            new QName(SOAP_ENV_URI, "Client");
    public static final QName  FAULTCODE_SERVER = 
            new QName(SOAP_ENV_URI, "Server");

    public static final QName HEADER_MUSTUNDERSTAND = 
            new QName(SOAP_ENV_URI, "mustUnderstand");
    
}
