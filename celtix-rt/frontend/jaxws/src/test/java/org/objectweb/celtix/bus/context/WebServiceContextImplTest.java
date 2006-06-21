package org.objectweb.celtix.bus.context;



import javax.xml.ws.handler.MessageContext;
import junit.framework.TestCase;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.WebServiceContextImpl;


public class WebServiceContextImplTest extends TestCase {

    public void tearDown() { 
        WebServiceContextImpl.clear();
    } 


    public void testConstructor() { 

        GenericMessageContext gmc = new GenericMessageContext(); 
        WebServiceContextImpl ctx = new WebServiceContextImpl(gmc);
        assertSame(gmc, ctx.getMessageContext());
    } 
    

    public void testGetSetMessageContext() { 

        WebServiceContextImpl wsci = new WebServiceContextImpl(); 
        assertNull(wsci.getMessageContext());

        
        final MessageContext ctx = new GenericMessageContext();
        WebServiceContextImpl.setMessageContext(ctx);

        assertSame(ctx, wsci.getMessageContext());

        Thread t = new Thread() { 
                public void run() {
                    WebServiceContextImpl threadLocalWSCI = new WebServiceContextImpl(); 

                    assertNull(threadLocalWSCI.getMessageContext());

                    MessageContext threadLocalCtx = new GenericMessageContext(); 
                    WebServiceContextImpl.setMessageContext(threadLocalCtx);


                    assertSame(threadLocalCtx, threadLocalWSCI.getMessageContext());
                    assertTrue(ctx !=  threadLocalWSCI.getMessageContext());
                    
                }
            };

        t.start(); 
        
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    } 
}
