package org.objectweb.celtix.systest.handlers;


import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.StreamMessageContext;
import org.objectweb.celtix.handlers.StreamHandler;

public class  TestStreamHandler extends TestHandlerBase 
    implements StreamHandler {

    private static final Logger LOG = Logger.getLogger(TestStreamHandler.class.getName()); 

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
        printHandlerInfo("handleMessage", isOutbound(ctx));

        if (isServerSideHandler()) { 
            if (!isOutbound(ctx)) { 
                getHandlerInfoList(ctx).add(getHandlerId());
            } else { 
                LOG.info("compressing message stream");
                // compress outbound on server side
                setupCompressionOutputStream(ctx);
            } 
        } else {  
            if (!isOutbound(ctx)) { 
                LOG.info("decompressing message stream");
                // decompress inbound on client side
                setupDecompressionInputStream(ctx); 
            } 
        } 
        return true;
    }


    public final boolean handleFault(StreamMessageContext ctx) {
        methodCalled("handleFault"); 
        printHandlerInfo("handleFault", isOutbound(ctx));
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

    private void setupDecompressionInputStream(StreamMessageContext ctx) { 
        try { 
            
            GZIPInputStream zipIn = new GZIPInputStream(ctx.getInputStream());
            ctx.setInputStream(zipIn); 
        } catch (IOException ex) { 
            throw new ProtocolException(ex);
        }
    } 

    private void setupCompressionOutputStream(StreamMessageContext ctx) { 

        try { 
            GZIPOutputStream zipOut = new GZIPOutputStream(ctx.getOutputStream()) {
                    public void flush() throws IOException { 
                        super.finish();
                        super.flush();
                    } 
                };

            ctx.setOutputStream(zipOut); 
        } catch (IOException ex) { 
            throw new ProtocolException(ex);
        }
    } 

    private boolean isOutbound(MessageContext ctx) {
        return (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    }
}
