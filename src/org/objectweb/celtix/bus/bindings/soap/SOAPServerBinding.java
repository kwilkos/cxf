package org.objectweb.celtix.bus.bindings.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public class SOAPServerBinding extends AbstractServerBinding {

    private static Logger logger = Logger.getLogger(SOAPServerBinding.class.getName());

    protected final SOAPBindingImpl soapBinding;

    public SOAPServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
        super(b, ref, ep);
        soapBinding = new SOAPBindingImpl();
    }

    public boolean isCompatibleWithAddress(String address) {
        URL url = null;

        try {
            url = new URL(address);
        } catch (MalformedURLException ex) {
            logger.severe("Invalid address:\n" + ex.getMessage());
        }
        String protocol = url.getProtocol();
        return "http".equals(protocol) || "https".equals(protocol);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bindings.AbstractServerBinding#getDefaultTransportFactory(java.lang.String)
     */
    @Override
    protected TransportFactory getDefaultTransportFactory(String address) {
        TransportFactoryManager tfm = bus.getTransportFactoryManager();
        try {
            return tfm.getTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/");
        } catch (BusException ex) {
            logger.severe("Failed to get default transport factory for SOAP server binding.");
        }
        return null;
    }

}
