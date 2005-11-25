package demo.streams.common;




import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.StreamMessageContext;
import org.objectweb.celtix.handlers.StreamHandler;

public class  CompressionStreamHandler implements StreamHandler {

    private static final Logger LOG = Logger.getLogger(CompressionStreamHandler.class.getName()); 


    public final boolean handleMessage(StreamMessageContext ctx) {

        setUpStream(ctx);
        return true;
    }


    public final boolean handleFault(StreamMessageContext ctx) {
        setUpStream(ctx);
        return true;
    }

    public final void init(final Map map) {
        System.out.println("CompressionStreamHandler initialised");
    }

    public final void destroy() {
    }

    public final void close(MessageContext messageContext) {
        System.out.println("CompressionStreamHandler closed");
    }


    private void setUpStream(StreamMessageContext ctx) { 
        if (isOutbound(ctx)) { 
            LOG.info("encrypting message stream");
            // compress outbound on server side
            setupCompressionOutputStream(ctx);
        } else {
            LOG.info("decrypting message stream");
            // decompress inbound on client side
            setupDecompressionInputStream(ctx); 
        } 
    } 

    private void setupDecompressionInputStream(StreamMessageContext ctx) { 
        
        try { 
            ctx.setInputStream(new GZIPInputStream(ctx.getInputStream())); 
        } catch (IOException ex) {
            throw new ProtocolException(ex);
        }
    }

    private void setupCompressionOutputStream(StreamMessageContext ctx) { 
        
        try { 
            ctx.setOutputStream(new GZIPOutputStream(ctx.getOutputStream()));
        } catch (IOException ex) {
            throw new ProtocolException(ex);
        }
    } 


    private boolean isOutbound(MessageContext ctx) {
        return (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    }

}
