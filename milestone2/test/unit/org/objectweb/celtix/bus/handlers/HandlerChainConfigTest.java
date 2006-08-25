package org.objectweb.celtix.bus.handlers;



import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.ws.WebServiceException;
import junit.framework.TestCase;


public class HandlerChainConfigTest extends TestCase {
    
    private static final String HANDLER_NAME_BASE = "DummyHandler";
    private static final String HANDLER_CLASSNAME = DummyHandler.class.getName();

    private InputStream in; 

    public void setUp() {
        in = getClass().getResourceAsStream(getName() + ".xml"); 
        assertNotNull("could not load config file", in); 
    } 


    public void testReadConfig() throws IOException { 
        
        HandlerChainConfig cfg = new HandlerChainConfig(in); 
        
        List<HandlerConfig> config = cfg.getHandlerConfig("TestHandlerChain"); 
        assertNotNull(config);
        assertEquals(2, config.size()); 
        int i = 0;
        for (HandlerConfig hc : config) {
            assertEquals(HANDLER_NAME_BASE + i, hc.getName()); 
            assertEquals(HANDLER_CLASSNAME, hc.getClassName()); 
            i++;
        } 
    } 


    public void testConfigNoName() throws IOException { 

        doInvalidXmlTest(""); 
    }

    public void testConfigNoClass() throws IOException { 

        doInvalidXmlTest(""); 
    }

    private void doInvalidXmlTest(String expectedErrorCode) throws IOException { 
        try { 
            new HandlerChainConfig(in); 
            fail("did not get expected exception"); 
        } catch (WebServiceException ex) { 
            // happy
        } 
    } 
}
