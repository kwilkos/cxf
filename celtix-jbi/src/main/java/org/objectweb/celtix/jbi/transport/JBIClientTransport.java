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
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.jws.WebService;
import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;

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
    
    private static final Logger LOG = Logger.getLogger(JBIClientTransport.class.getName());
    private final DeliveryChannel channel; 
    private final EndpointReferenceType endpointRef; 
    private final QName serviceName;
    
    public JBIClientTransport(DeliveryChannel dc, EndpointReferenceType epr) { 
        channel = dc;
        endpointRef = epr;
        serviceName = EndpointReferenceUtils.getServiceName(endpointRef);
    } 
    
    public void invokeOneway(OutputStreamMessageContext context) throws IOException {
        throw new RuntimeException("not yet implemented");
    }
    
    public InputStreamMessageContext invoke(OutputStreamMessageContext context)
        throws IOException { 
        
        try { 
            Method targetMethod = (Method)context.get(ObjectMessageContext.METHOD_OBJ);
            Class<?> clz = targetMethod.getDeclaringClass(); 
            
            LOG.fine("invoking service " + clz);
            
            WebService ws = clz.getAnnotation(WebService.class);
            assert ws != null;
            QName interfaceName = new QName(ws.targetNamespace(), ws.name());
            
            MessageExchangeFactory factory = channel.createExchangeFactoryForService(serviceName);
            LOG.fine("create message exchange svc: " + serviceName);
            InOut xchng = factory.createInOutExchange();
            
            NormalizedMessage inMsg = xchng.createMessage();
            LOG.fine("exchange endpoint: " + xchng.getEndpoint());
            
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
                System.out.println("no message yet");
            } 
            
            if (ins == null) { 
                throw new IOException("unable to retrieve message");
            } 
            return new JBIInputStreamMessageContext(context, ins);
            
        } catch (Exception ex) { 
            ex.printStackTrace();
            throw new IOException(ex.toString());
        } 
    } 
    
    Source getMessageContent(OutputStreamMessageContext context) {
        assert context instanceof JBIOutputStreamMessageContext 
            : "context must be of type JBIOutputStreamMessageContext";
    
        JBIOutputStreamMessageContext ctx = (JBIOutputStreamMessageContext)context;
        ByteArrayOutputStream bos = (ByteArrayOutputStream)ctx.getOutputStream();
        return new StreamSource(new ByteArrayInputStream(bos.toByteArray()));
    } 
    
    
    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, 
                                                         Executor executor) 
        throws IOException { 
        throw new RuntimeException("not yet implemented");
    }
    
    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) 
        throws IOException { 
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
