package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;


public class SOAPClientBinding extends AbstractClientBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(SOAPClientBinding.class);
    protected final SOAPBindingImpl soapBinding;
    
    public SOAPClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        soapBinding = new SOAPBindingImpl();
    }
    
    public Binding getBinding() {
        return soapBinding;
    }
    
    public boolean isCompatibleWithAddress(String address) {
        return soapBinding.isCompatibleWithAddress(address);
    }

    protected MessageContext createBindingMessageContext(MessageContext ctx) {
        return new SOAPMessageContextImpl(ctx);
    }

    protected void marshal(ObjectMessageContext objContext, MessageContext context) {
        try {
            SOAPMessage msg = soapBinding.marshalMessage(objContext, context);
            ((SOAPMessageContext)context).setMessage(msg);
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SOAP_MARSHALLING_FAILURE_MSG", se);
            throw new ProtocolException(se.getMessage());
        }
    }

    protected void unmarshal(MessageContext context, ObjectMessageContext objContext) {
        try {
            soapBinding.unmarshalMessage(context, objContext);
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SOAP_UNMARSHALLING_FAILURE_MSG", se);
            throw new ProtocolException(se.getMessage());
        }
    }

    protected boolean hasFault(MessageContext context) {
        SOAPMessage msg = ((SOAPMessageContext)context).getMessage();
        assert msg != null;
        boolean hasFault = false;
        try {
            hasFault = msg.getSOAPBody().hasFault();
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SOAP_UNMARSHALLING_FAILURE_MSG", se);
            throw new ProtocolException(se.getMessage());
        }
        
        return hasFault;
    }
    
    protected void unmarshalFault(MessageContext context, ObjectMessageContext objContext) {
        try {
            soapBinding.unmarshalFault(context, objContext);
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SOAP_UNMARSHALLING_FAILURE_MSG", se);
            throw new ProtocolException(se.getMessage());
        }
    }
    
    protected void write(MessageContext context, OutputStreamMessageContext outCtx) {
        SOAPMessageContext soapCtx = (SOAPMessageContext)context;
        try {
            soapCtx.getMessage().writeTo(outCtx.getOutputStream());
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SOAP_WRITE_FAILURE_MSG", se);
            throw new ProtocolException(se.getMessage());
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "SOAP_WRITE_IO_FAILURE_MSG", ioe);
            throw new ProtocolException(ioe.getMessage());
        }
    }

    protected void read(InputStreamMessageContext inCtx, MessageContext context) {
        try {
            soapBinding.parseMessage(inCtx.getInputStream(), context);
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SOAP_PARSING_FAILURE_MSG", se);
            throw new ProtocolException(se.getMessage());
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "SOAP_READ_IO_FAILURE_MSG", ioe);
            throw new ProtocolException(ioe.getMessage());
        }
    }
}
