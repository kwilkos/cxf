package org.objectweb.celtix.bus.context;


import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.context.GenericMessageContext; 
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.context.StreamMessageContext;


public class StreamMessageContextImplTest extends AbstractMessageContextTestBase
{
    private final GenericMessageContext base = new GenericMessageContext();  

    @Override
    protected MessageContext getMessageContext() { 
        return new StreamMessageContextImpl(new ToyInputStreamMessageContext(base)); 
    } 

    public void testContextWithInputStream() { 
        
        InputStream inStream = EasyMock.createMock(InputStream.class);
        InputStreamMessageContext inctx = EasyMock.createMock(InputStreamMessageContext.class);
        
        inctx.getInputStream(); 
        EasyMock.expectLastCall().andReturn(inStream);

        StreamMessageContext ctx = new StreamMessageContextImpl(inctx);
        
        EasyMock.replay(inctx);
        assertSame(inStream, ctx.getInputStream());
        EasyMock.verify(inctx); 

        EasyMock.reset(inctx);
        InputStream in2 = EasyMock.createMock(InputStream.class); 
        inctx.setInputStream(in2);
        EasyMock.replay(inctx);
        ctx.setInputStream(in2); 

        EasyMock.verify(inctx); 

    } 

    public void testContextWithOutputStream() { 

        OutputStream outStream = EasyMock.createMock(OutputStream.class);
        OutputStreamMessageContext outctx = EasyMock.createMock(OutputStreamMessageContext.class);
        
        outctx.getOutputStream(); 
        EasyMock.expectLastCall().andReturn(outStream);

        StreamMessageContext ctx = new StreamMessageContextImpl(outctx);
        
        EasyMock.replay(outctx);
        assertSame(outStream, ctx.getOutputStream());
        EasyMock.verify(outctx); 

        EasyMock.reset(outctx);
        OutputStream out2 = EasyMock.createMock(OutputStream.class); 
        outctx.setOutputStream(out2);
        EasyMock.replay(outctx);
        ctx.setOutputStream(out2); 

        EasyMock.verify(outctx); 
    } 

    
    public void testContextWithOutputStreamInvokeInputStream() { 

        OutputStreamMessageContext outctx = EasyMock.createMock(OutputStreamMessageContext.class);
        StreamMessageContext ctx = new StreamMessageContextImpl(outctx);

        try { 
            ctx.getInputStream();
            fail("did not get expected exception"); 
        } catch (IllegalStateException ex) { 
            // expected
        } 

        try { 
            ctx.setInputStream(EasyMock.createMock(InputStream.class));
            fail("did not get expected exception"); 
        } catch (IllegalStateException ex) { 
            // expected
        } 
    } 


    public void testContextWithInputStreamInvokeOutputStream() { 

        InputStreamMessageContext inctx = EasyMock.createMock(InputStreamMessageContext.class);
        StreamMessageContext ctx = new StreamMessageContextImpl(inctx);

        try { 
            ctx.getOutputStream();
            fail("did not get expected exception"); 
        } catch (IllegalStateException ex) { 
            // expected
        } 

        try { 
            ctx.setOutputStream(EasyMock.createMock(OutputStream.class));
            fail("did not get expected exception"); 
        } catch (IllegalStateException ex) { 
            // expected
        } 
    } 

    public void testContrcutorNullInput() { 

        try { 
            new StreamMessageContextImpl((InputStreamMessageContext)null); 
            fail("did not get expected expcetion");
        } catch (NullPointerException ex) { 
            // expected
        } 
    } 


    public void testContrcutorNullOutput() { 

        try { 
            new StreamMessageContextImpl((OutputStreamMessageContext)null); 
            fail("did not get expected expcetion");
        } catch (NullPointerException ex) { 
            // expected
        } 
    } 

    static class ToyInputStreamMessageContext extends MessageContextWrapper 
        implements InputStreamMessageContext {
        private static final long serialVersionUID = 1;
        
        ToyInputStreamMessageContext(MessageContext wrapped) { 
            super(wrapped); 
        } 

        public InputStream getInputStream() { 
            return null; 
        }
    
        public void setInputStream(InputStream ins) {
        }
        
        public boolean isFault() { 
            return false; 
        } 

        public void setFault(boolean b) {
        } 
    }

}