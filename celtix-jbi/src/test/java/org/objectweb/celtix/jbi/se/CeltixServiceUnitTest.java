package org.objectweb.celtix.jbi.se;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessagingException;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.jbi.transport.JBITransportFactory;

public class CeltixServiceUnitTest extends TestCase {
    
    private static final Logger LOG = Logger.getLogger(CeltixServiceUnitTest.class.getName());
    private static final String ROOT_PATH = 
        "/service-assemblies/celtix-demo-service-assembly/version_1/sus/" 
            + "CeltixServiceEngine/JBIDemoSE_AProvider";
    private static final String CELTIX_CONFIG = 
        "/components/CeltixServiceEngine/version_1/META-INF/celtix-config.xml";
    private CeltixServiceUnit csu;
    private CeltixServiceUnitManager csuManager;
    private ComponentContext ctx = EasyMock.createMock(ComponentContext.class);
    private DeliveryChannel channel = EasyMock.createMock(DeliveryChannel.class);
    private String absCsuPath;
    private Bus bus;    
    
    public void setUp() throws Exception {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        System.setProperty("celtix.config.file", getClass().getResource(CELTIX_CONFIG).toString());
        absCsuPath = getClass().getResource(ROOT_PATH).getFile();
        bus = Bus.init();
               
                
        ComponentClassLoader componentClassLoader = 
            new ComponentClassLoader(new URL[0], getClass().getClassLoader());  
         
        csuManager = new CeltixServiceUnitManager(bus, ctx, componentClassLoader);
        
        csu = new CeltixServiceUnit(bus, absCsuPath, componentClassLoader);
        
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testPrepare() throws Exception {
        registerJBITransport(bus, csuManager);
        channel.accept();
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.replay(channel);
        csu.prepare(ctx);
        assertTrue(csu.isServiceProvider());
        assertEquals(csu.getEndpointName(), "SE_Endpoint");
    }
    
    
    private void registerJBITransport(Bus argBus, CeltixServiceUnitManager mgr) throws JBIException { 
        try { 
           
            getTransportFactory().init(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
        } catch (Exception ex) {
            throw new JBIException("failed to register JBI transport factory", ex);
        }
    } 
    
    private JBITransportFactory getTransportFactory() throws BusException, MessagingException { 
        assert bus != null;
        
        try { 
            JBITransportFactory transportFactory = 
                (JBITransportFactory)bus.getTransportFactoryManager()
                    .getTransportFactory(CeltixServiceEngine.JBI_TRANSPORT_ID);
            transportFactory.setDeliveryChannel(channel);
            return transportFactory;
        } catch (BusException ex) { 
            LOG.log(Level.SEVERE, "error initializing bus", ex);
            throw ex;
        } 
    }
    
    
}
