package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public abstract class GenericClientBinding implements ClientBinding {
    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected ClientTransport transport;
    
    public GenericClientBinding(Bus b, EndpointReferenceType ref) {
        bus = b;
        reference = ref;
        transport = createTransport();
    }

    protected ClientTransport createTransport() {
        try {
            Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
            List<?> exts = port.getExtensibilityElements();
            if (exts.size() > 0) {
                ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                transport = bus.getTransportFactoryManager()
                    .getTransportFactory(el.getElementType().getNamespaceURI())
                    .createClientTransport(reference);
            }
        } catch (Exception ex) {
            // TODO - exception handling
        }
        return null;
    }


    public ObjectMessageContext createObjectContext() {
        return new GenericObjectContext();
    }

    protected abstract  MessageContext createBindingMessageContext();
    
    protected abstract void marshal(ObjectMessageContext objContext, MessageContext context);

    protected abstract void unmarshal(MessageContext context, ObjectMessageContext objContext);

    protected abstract void write(MessageContext context, OutputStreamMessageContext outCtx);

    protected abstract void read(InputStreamMessageContext inCtx, MessageContext context);
    
    public ObjectMessageContext invoke(ObjectMessageContext context) throws IOException {
        // TODO - invoke ObjectMessageContext handlers
        MessageContext bindingContext = createBindingMessageContext();
        
        if (null != bindingContext) {
            marshal(context, bindingContext);
        } else {
            bindingContext = context;
        }

        OutputStreamMessageContext ostreamContext = transport.createOutputStreamContext(bindingContext);
        // TODO - invoke output stream handlers
        transport.finalPrepareOutputStreamContext(ostreamContext);
        
        write(bindingContext, ostreamContext);

        InputStreamMessageContext ins = transport.invoke(ostreamContext);
       
        context = createObjectContext();
        bindingContext = createBindingMessageContext();
        if (null == bindingContext) {
            bindingContext = context;
        }
           
        // TODO - invoke input stream handlers
        read(ins, bindingContext);
        
        // TODO - invoke binding handlers
        unmarshal(bindingContext, context);
        
        // TODO - invoke object handlers
        
        return context;
    }

    
    public void invokeOneWay(ObjectMessageContext context) {
        // TODO Auto-generated method stub

    }

    public Future<ObjectMessageContext> invokeAsync(ObjectMessageContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    
    static class GenericObjectContext extends HashMap<String, Object> implements ObjectMessageContext {
        static final String OBJECT_KEY = GenericObjectContext.class.getName();
        private static final long serialVersionUID = 401275179632507389L;

        
        public Object[] getMessageObjects() {
            return (Object[])get(OBJECT_KEY);
        }

        public void setMessageObjects(Object... objects) {
            put(OBJECT_KEY, (Object)objects);
        }

        public Scope getScope(String name) {
            return (Scope)get(OBJECT_KEY + ".Scope");
        }

        public void setScope(String name, MessageContext.Scope scope) {
            put(name + ".Scope", scope);
        }        
    }
}
