package org.objectweb.celtix.handlers; 

import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;



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
    boolean invokeLogicalHandlers(boolean requestor);
        
    /** 
     * Invoke the protocol handlers. 
     * 
     * @param requestor true if being invoked on the request initiator
     * @param bindingContext binding specific MessageContext
     */
    boolean invokeProtocolHandlers(boolean requestor, MessageContext bindingContext);
    
    
    /** 
     * Invoke the stream level handlers. 
     */
    boolean invokeStreamHandlers();
        
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
    boolean faultRaised();

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
     * set the current exception in this message exchange
     */
    void setFault(Exception pe);

    /** 
     * Invoke handlers at the end of an MEP calling close on each.
     */
    void mepComplete();


    /** 
     * Indicates that the invoker is closed.  When closed, only *
     * #mepComplete may be called.  The invoker will become closed if
     * during a invocation of handlers, a handler throws a runtime
     * exception that is not a protocol exception and no futher
     * handler or message processing is possible.
     *
     */
    boolean isClosed();

    /** 
     * get the context associated with this invoker
     *
     * @return the associated context
     */
    ObjectMessageContext getContext();

    /** 
     * assoociate a context with this invoker
     *
     * @param ctx the associated context
     */
    void setContext(ObjectMessageContext ctx);
}

