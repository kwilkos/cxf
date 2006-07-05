package org.objectweb.celtix.jbi.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.jws.WebService;
import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;



/**
 * Connects Celtix clients to the NormalizedMessageRouter.  Celtix
 * messages are wrapped in a NormalizedMessage before being sent to
 * the NMR and are unwrapped when being received from it.
 */
public class JBIClientTransport implements ClientTransport {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JBIClientTransport.class);
    private final DeliveryChannel channel; 
    private final EndpointReferenceType endpointRef; 
    private final QName serviceName;
    private final ResponseCallback responseCallback;
    
    public JBIClientTransport(DeliveryChannel dc, 
                              EndpointReferenceType epr,
                              ClientBinding binding) { 
        channel = dc;
        endpointRef = epr;
        serviceName = EndpointReferenceUtils.getServiceName(endpointRef);
        responseCallback = binding.createResponseCallback();
    } 
    
    public void invokeOneway(OutputStreamMessageContext context) throws IOException {
        try {
            LOG.fine("it's in-only invokation");
            Method targetMethod = (Method)context.get(ObjectMessageContext.METHOD_OBJ);
            Class<?> clz = targetMethod.getDeclaringClass(); 
            
            LOG.fine(new Message("INVOKE.SERVICE", LOG).toString() + clz);
            
            WebService ws = clz.getAnnotation(WebService.class);
            assert ws != null;
            QName interfaceName = new QName(ws.targetNamespace(), ws.name());
            
            MessageExchangeFactory factory = channel.createExchangeFactoryForService(serviceName);
            LOG.fine(new Message("CREATE.MESSAGE.EXCHANGE", LOG).toString() + serviceName);
            InOnly xchng = factory.createInOnlyExchange();
            
            NormalizedMessage inMsg = xchng.createMessage();
            LOG.fine(new Message("EXCHANGE.ENDPOINT", LOG).toString() + xchng.getEndpoint());
            
            if (inMsg != null) { 
                LOG.fine("setup message contents on " + inMsg);
                inMsg.setContent(getMessageContent(context));
                xchng.setService(serviceName); 
                LOG.fine("service for exchange " + serviceName);
                
                xchng.setInterfaceName(interfaceName); 
                
                xchng.setOperation(new QName(targetMethod.getName())); 
                xchng.setInMessage(inMsg);
                LOG.fine("sending message");
                channel.send(xchng);
                
                                
            } else { 
                LOG.fine(new Message("NO.MESSAGE", LOG).toString());
            } 
            
                        
        } catch (Exception ex) { 
            ex.printStackTrace();
            throw new IOException(ex.toString());
        } 
    }
    
    public InputStreamMessageContext invoke(OutputStreamMessageContext context)
        throws IOException { 
        
        try {
            LOG.fine("it's in-out invokation");
            Method targetMethod = (Method)context.get(ObjectMessageContext.METHOD_OBJ);
            Class<?> clz = targetMethod.getDeclaringClass(); 
            
            LOG.fine(new Message("INVOKE.SERVICE", LOG).toString() + clz);
            
            WebService ws = clz.getAnnotation(WebService.class);
            assert ws != null;
            QName interfaceName = new QName(ws.targetNamespace(), ws.name());
            
            MessageExchangeFactory factory = channel.createExchangeFactoryForService(serviceName);
            LOG.fine(new Message("CREATE.MESSAGE.EXCHANGE", LOG).toString() + serviceName);
            InOut xchng = factory.createInOutExchange();
            
            NormalizedMessage inMsg = xchng.createMessage();
            LOG.fine(new Message("EXCHANGE.ENDPOINT", LOG).toString() + xchng.getEndpoint());
            
            InputStream ins = null;
            
            if (inMsg != null) { 
                LOG.fine("setup message contents on " + inMsg);
                inMsg.setContent(getMessageContent(context));
                xchng.setService(serviceName); 
                LOG.fine("service for exchange " + serviceName);
                
                xchng.setInterfaceName(interfaceName); 
                
                xchng.setOperation(new QName(targetMethod.getName())); 
                xchng.setInMessage(inMsg);
                LOG.fine("sending message");
                channel.sendSync(xchng);
                
                NormalizedMessage outMsg = xchng.getOutMessage();
                ins = JBIMessageHelper.convertMessageToInputStream(outMsg.getContent());
                
            } else { 
                LOG.fine(new Message("NO.MESSAGE", LOG).toString());
            } 
            
            if (ins == null) { 
                throw new IOException(new Message("UNABLE.RETRIEVE.MESSAGE", LOG).toString());
            } 
            return new JBIInputStreamMessageContext(context, ins);
            
        } catch (Exception ex) { 
            ex.printStackTrace();
            throw new IOException(ex.toString());
        } 
    } 
    
    Source getMessageContent(OutputStreamMessageContext context) {
        assert context instanceof JBIOutputStreamMessageContext 
            : new Message("CONTEXT.MUST.BE", LOG).toString();
    
        JBIOutputStreamMessageContext ctx = (JBIOutputStreamMessageContext)context;
        ByteArrayOutputStream bos = (ByteArrayOutputStream)ctx.getOutputStream();
        return new StreamSource(new ByteArrayInputStream(bos.toByteArray()));
    } 
    
    
    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, 
                                                         Executor executor) 
        throws IOException { 
        throw new RuntimeException(new Message("NOT.IMPLEMENTED", LOG).toString());
    }
    
    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) 
        throws IOException { 
    }
    
    public ResponseCallback getResponseCallback() {
        return responseCallback;
    }
    
    public void shutdown() {
    }
    
    
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context)
        throws IOException {
        return new JBIOutputStreamMessageContext(context);
    }
    
    public EndpointReferenceType getTargetEndpoint() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public EndpointReferenceType getDecoupledEndpoint() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Port getPort() {
        // TODO Auto-generated method stub
        return null;
    }
}
