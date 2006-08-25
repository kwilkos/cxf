package org.objectweb.celtix.handlers; 


import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;



/** 
 * Invokes the handlers associated with a binding.  The client and
 * server bindings invoke their handlers using the HandlerInvoker.
 * For details of how the invokers should be invoked, see the JAXWS
 * 2.0 specification.
 */
public interface HandlerInvoker {

    /** 
     * Invoke the logical handlers. 
     * 
     * @param requestor true if being invoked on the request initiator
     */
    boolean invokeLogicalHandlers(boolean requestor, ObjectMessageContext objectContext);
        
    /** 
     * Invoke the protocol handlers. 
     * 
     * @param requestor true if being invoked on the request initiator
     * @param bindingContext binding specific MessageContext
     */
    boolean invokeProtocolHandlers(boolean requestor, MessageContext bindingContext);
    
     
    /** 
     * Invoke the stream level handlers with an InputStream.
     * 
     * @param context the InputStreamMessageContext for the message
     * exchange
     */
    boolean invokeStreamHandlers(InputStreamMessageContext context);
        

    /** 
     * Invoke the stream level handlers with an OutputStream.
     *
     * @param context the OutputStreamMessageContext for the message exchange
     */
    boolean invokeStreamHandlers(OutputStreamMessageContext context);

    /** 
     * Close all handlers that have previously invoked
     */
    void closeHandlers();
    
    /** 
     * Indicates if a fault has been raised
     *
     * @return true if an exception has been thrown by an invoked
     * handler.
     */
    boolean faultRaised(MessageContext context);

    /** 
     * Is the current message direction outbound
     *
     * @return true if current message direction is outbound
     */
    boolean isOutbound();
    
    /** 
     * Is the current message direction inbound
     *
     * @return true if current message direction is inbound
     */
    boolean isInbound();
    
    
    /** 
     * set the current message direction to inbound
     */
    void setInbound(); 

    /** 
     * set the current message direction to outabound
     */
    void setOutbound();

    /**
     * set the invoker into fault processing mode.  This method is
     * invoked when a client transport indicates that a fault has been
     * raised by the server but the message has not yet been read or
     * unmarshalled.
     */
    void setFault(boolean faultExpected);

    /** 
     * Invoke handlers at the end of an MEP calling close on each.
     */
    void mepComplete(MessageContext context);


    /** 
     * Indicates that the invoker is closed.  When closed, only *
     * #mepComplete may be called.  The invoker will become closed if
     * during a invocation of handlers, a handler throws a runtime
     * exception that is not a protocol exception and no futher
     * handler or message processing is possible.
     *
     */
    boolean isClosed();
}

