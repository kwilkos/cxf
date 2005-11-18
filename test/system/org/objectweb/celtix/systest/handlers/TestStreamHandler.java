package org.objectweb.celtix.systest.handlers;


import java.util.Map;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.StreamMessageContext;
import org.objectweb.celtix.handlers.StreamHandler;

public class  TestStreamHandler extends TestHandlerBase 
    implements StreamHandler {

    public TestStreamHandler() {
        this(true); 
    } 

    public TestStreamHandler(boolean serverSide) {
        super(serverSide);
    }

    public String getHandlerId() { 
        return "streamHandler" + getId();
    }
    
    public final boolean handleMessage(StreamMessageContext ctx) {

        methodCalled("handleMessage"); 

        if (isServerSideHandler()) { 
            if (!isOutbound(ctx)) { 
                getHandlerInfoList(ctx).add(getHandlerId());
            } else { 
                // compress outbound on server side
                //setupCompressionOutputStream(ctx);
            } 
        } else {  
            if (!isOutbound(ctx)) { 
                // decompress inbound on client side
                //setupDecompressionInputStream(ctx); 
            } 
        } 
        return true;
    }


    public final boolean handleFault(StreamMessageContext ctx) {
        methodCalled("handleFault"); 
        return true;
    }

    public final void init(final Map map) {
        methodCalled("init"); 
    }

    public final void destroy() {
        methodCalled("destroy"); 
    }

    public final void close(MessageContext messageContext) {
        methodCalled("close"); 
    }


    public String toString() { 
        return getHandlerId();
    } 


    private boolean isOutbound(MessageContext ctx) {
        return (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    }
}
