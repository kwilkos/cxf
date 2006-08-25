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
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.transport.JBITransportFactory;

public class CeltixServiceUnitTest extends TestCase {
    
    private static final Logger LOG = LogUtils.getL7dLogger(CeltixServiceUnitTest.class);
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
        registerJBITransport(bus, csuManager);
        channel.accept();
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.replay(channel);
    }
    
    public void tearDown() throws Exception {
        bus.shutdown(false);
    }
    
    public void testPrepare() throws Exception {
        csu.prepare(ctx);
        assertTrue(csu.isServiceProvider());
        assertEquals(csu.getServiceName().getNamespaceURI(), "http://objectweb.org/hello_world");
        assertEquals(csu.getServiceName().getLocalPart(), "HelloWorldService");
    }
    
    public void testEndpintName() throws Exception {
        assertEquals(csu.getEndpointName(), "SE_Endpoint");
    }
    
    public void testServiceName() throws Exception {
        csu.prepare(ctx);
        assertEquals(csu.getServiceName().getNamespaceURI(), "http://objectweb.org/hello_world");
        assertEquals(csu.getServiceName().getLocalPart(), "HelloWorldService");
    }
    
    public void testStart() throws Exception {
        
    }
    
    public void testStop() throws Exception {
        
    }
    
    private void registerJBITransport(Bus argBus, CeltixServiceUnitManager mgr) throws JBIException { 
        try { 
           
            getTransportFactory().init(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
        } catch (Exception ex) {
            throw new JBIException(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", LOG).toString(), ex);
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
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
            throw ex;
        } 
    }
      
}
