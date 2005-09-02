package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public class SOAPServerBinding extends AbstractServerBinding {

    private static Logger logger = Logger.getLogger(SOAPServerBinding.class.getName());

    protected final SOAPBindingImpl soapBinding;

    public SOAPServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
        super(b, ref, ep);
        soapBinding = new SOAPBindingImpl();
    }
    
    public Binding getBinding() {
        return soapBinding;
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

    protected TransportFactory getDefaultTransportFactory(String address) {
        TransportFactoryManager tfm = bus.getTransportFactoryManager();
        try {
            return tfm.getTransportFactory("http://schemas.xmlsoap.org/wsdl/soap/");
        } catch (BusException ex) {
            logger.severe("Failed to get default transport factory for SOAP server binding.");
        }
        return null;
    }
    
    // REVISIT: 
    // the following are identical to implementations in SOAPCLientBinding
    // and needs to be refactored

    protected MessageContext createBindingMessageContext() {
        return new SOAPMessageContextImpl();
    }

    protected void marshal(ObjectMessageContext objContext, MessageContext context) {
        // TODO Marshall Objects to SAAJ using JAXB
        //Create a SOAP Message
        try {
            SOAPMessage msg = soapBinding.buildSoapInputMessage(objContext);
            ((SOAPMessageContext)context).setMessage(msg);
        } catch (SOAPException se) {
            //TODO
        }
    }

    protected void unmarshal(MessageContext context, ObjectMessageContext objContext) {
        super.unmarshal(context,  objContext);
        //TODO UnMarshall SAAJ to Objects using JAXB
    }

    protected void write(MessageContext context, 
            OutputStreamMessageContext outCtx) throws IOException {
        SOAPMessageContext soapCtx = (SOAPMessageContext)context;
        try {
            soapCtx.getMessage().writeTo(outCtx.getOutputStream());        
        } catch (SOAPException se) {
            throw new IOException(se.getMessage());
        }
    }

    protected void read(InputStreamMessageContext instr,
            MessageContext mc) throws IOException {
        //TODO Read Stream into SOAP Message using SAAJ API
    }
    

}
