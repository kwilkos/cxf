package org.objectweb.celtix.bus.bindings.soap;

import java.net.URL;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.GenericClientBinding;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;


public class SOAPClientBinding extends GenericClientBinding {
    protected final SOAPBindingImpl soapBinding;
    
    public SOAPClientBinding(Bus b, EndpointReferenceType ref) {
        super(b, ref);
        soapBinding = new SOAPBindingImpl();
    }
    
    public Binding getBinding() {
        return soapBinding;
    }
    
    public boolean isCompatibleWithAddress(URL address) {
        String protocol = address.getProtocol();
        return "http".equals(protocol) || "https".equals(protocol);
    }

    protected MessageContext createBindingMessageContext() {
        return new SOAPMessageContextImpl();
    }

    protected void marshal(ObjectMessageContext objContext, MessageContext context) {
        //TODO Marshall Objects to SAAJ using JAXB
    }

    protected void unmarshal(MessageContext context, ObjectMessageContext objContext) {
        //TODO UnMarshall SAAJ to Objects using JAXB
    }

    protected void write(MessageContext context, OutputStreamMessageContext outCtx) {
        //TODO Write Soap Message SAAJ Model to Transport Stream.
    }

    protected void read(InputStreamMessageContext instr, MessageContext mc) {
        //TODO Read Stream into SOAP Message using SAAJ API
    }
}
