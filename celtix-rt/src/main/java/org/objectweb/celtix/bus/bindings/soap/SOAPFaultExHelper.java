package org.objectweb.celtix.bus.bindings.soap;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

public final class SOAPFaultExHelper {
    private SOAPFaultExHelper() {
        //Complete
    }
    
    public static SOAPFaultException createSOAPFaultEx(SOAPFactory soapFactory,
                                                       QName faultCode, 
                                                       Throwable cause) {

        StringBuffer str = new StringBuffer(cause.toString());
        str.append("\n");
        for (StackTraceElement s : cause.getStackTrace()) {
            str.append(s.toString());
            str.append("\n");
        }
        
        SOAPFaultException sfe = createSOAPFaultEx(soapFactory, faultCode, str.toString());
        sfe.initCause(cause);
        return sfe;
    }
    
    public static SOAPFaultException createSOAPFaultEx(SOAPFactory soapFactory, 
                                                       QName faultCode, 
                                                       String message) {
        SOAPFault sf = null;
        try {
            sf = soapFactory.createFault();
            sf.setFaultCode(faultCode);
            sf.setFaultString(message);            
        } catch (SOAPException se) {
            se.printStackTrace();            
        }
        return new SOAPFaultException(sf);
    }
    
}
