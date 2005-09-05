package org.objectweb.celtix.bus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class EndpointFactoryImpl extends javax.xml.ws.EndpointFactory {

    private static Logger logger = Logger.getLogger(EndpointFactoryImpl.class.getPackage().getName());

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.EndpointFactory#createEndpoint(java.net.URI,
     *      java.lang.Object)
     */
    @Override
    public Endpoint createEndpoint(URI bindingId, Object implementor) {
        Endpoint ep = null;
        if (EndpointUtils.isValidImplementor(implementor)) {
            try {
                ep = new EndpointImpl(Bus.getCurrent(), implementor, bindingId);
            } catch (BusException ex) {
                logger.severe("Failed to create endpoint:\n" + ex.getMessage());
            }
            return ep;
        }
        logger.severe("Cannot create Endpoint for implementor that does not have a WebService annotation\n"
                      + " and does not implement the Provider interface.");
        return null;
    }
    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.EndpointFactory#publish(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public Endpoint publish(String address, Object implementor) {

        URI bindingId = getDefaultBindingId(address);

        Endpoint ep = createEndpoint(bindingId, implementor);
        ep.publish(address);
        return ep;
    }

    URI getDefaultBindingId(String a) {
        URL address = null;
        try {
            address = new URL(a);
        } catch (MalformedURLException ex) {
            logger.severe("Could not obtain default endpoint binding for address " + a + "\n"
                          + ex.getMessage());
        }
        String protocol = address.getProtocol();
        if ("http".equals(protocol) || "https".equals(protocol)) {
            try {
                return new URI(SOAPBinding.SOAP11HTTP_BINDING);
            } catch (URISyntaxException ex) {
                // should never happen
                assert false;
            }
        }
        return null;
    }

    boolean checkAnnotations(Object implementor) {
        javax.jws.WebService wsAnnotation = (WebService)implementor.getClass()
            .getAnnotation(WebService.class);

        return null != wsAnnotation;
    }

}
