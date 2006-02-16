package org.objectweb.celtix.jbi.se;


import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.jbi.transport.JBITransportFactory;


/** A JBI component.  Initializes the Celtix JBI transport
 */
public class CeltixServiceEngine implements ComponentLifeCycle, Component {
    
    public static final String JBI_TRANSPORT_ID = "http://celtix.object.org/transport/jbi";
    private static final String CELTIX_CONFIG_FILE = "celtix-config.xml";
    private static final String PROVIDER_PROP = "javax.xml.ws.spi.Provider";
    
    
    private static final Logger LOG = Logger.getLogger(CeltixServiceEngine.class.getName());
    
    private ComponentContext ctx; 
    private CeltixServiceUnitManager suManager;
    private Bus bus; 
   
    public CeltixServiceEngine() {
    }
    
    // Implementation of javax.jbi.component.ComponentLifeCycle
    
    public final ObjectName getExtensionMBeanName() {
        return null;
    }
    
    public final void shutDown() throws JBIException {
        LOG.fine("Shutting down CeltixServiceEngine");
    }
    
    public final void init(final ComponentContext componentContext) throws JBIException {
        
        
        try { 
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            
            System.setProperty(PROVIDER_PROP, "org.objectweb.celtix.bus.jaxws.spi.ProviderImpl");
            ctx = componentContext;
            
            File metaInfDir = new File(componentContext.getInstallRoot(), "META-INF");
            File celtixConfig = new File(metaInfDir, CELTIX_CONFIG_FILE); 
            
            if (celtixConfig.exists()) { 
                System.setProperty("celtix.config.file", celtixConfig.toURL().toString());
                LOG.fine("set Celtix configuration to: " + System.getProperty("celtix.config.file"));
            } else { 
                LOG.severe("could not find Celtix configuration in " + metaInfDir);
            } 
            
            ComponentClassLoader loader = createClassLoader();
            
            initializeBus();
            suManager = new CeltixServiceUnitManager(bus, componentContext, loader);
            registerJBITransport(bus, suManager);
            
            LOG.info("Celtix Service Engine installation root:" + componentContext.getInstallRoot());
            LOG.info("CeltixServiceEngine init complete");
        } catch (Throwable ex) { 
            ex.printStackTrace();
            LOG.log(Level.SEVERE, "failed to initilialize bus", ex);
            throw new JBIException(ex);
        } 
    }
    
    
    private void initializeBus() throws JBIException { 
        
        try { 
            LOG.fine("initialising bus");
            bus = Bus.init();
            LOG.fine("init complete");
        } catch (Exception ex) { 
            LOG.log(Level.SEVERE, "bus initialization failed", ex);
            throw new JBIException(ex);
        } 
    } 
    
    
    public final void start() throws JBIException {
        
        try { 
            LOG.fine("CeltixServiceEngine starting");
            DeliveryChannel chnl = ctx.getDeliveryChannel();
            configureJBITransportFactory(chnl, suManager); 
            LOG.fine("CeltixServiceEngine startup complete");
        } catch (BusException ex) {
            throw new JBIException(ex);
        }
    }
    
    public final void stop() throws JBIException {
        LOG.fine("CeltixServiceEngine stopped");
    }
    
    // Implementation of javax.jbi.component.Component
    
    public final ComponentLifeCycle getLifeCycle() {
        LOG.fine("CeltixServiceEngine returning life cycle");
        return this;
    }
    
    public final ServiceUnitManager getServiceUnitManager() {
        LOG.fine("CeltixServiceEngine return service unit manager");
        return suManager;
    }
    
    public final Document getServiceDescription(final ServiceEndpoint serviceEndpoint) {
        Document doc = suManager.getServiceDescription(serviceEndpoint);
        LOG.fine("CeltixServiceEngine returning service description: " + doc);
        return doc;
    }
    
    public final boolean isExchangeWithConsumerOkay(final ServiceEndpoint ep, 
                                                    final MessageExchange exchg) {
        
        LOG.fine("isExchangeWithConsumerOkay: endpoint: " + ep 
                 + " exchange: " + exchg);
        return true;
    }
    
    public final boolean isExchangeWithProviderOkay(final ServiceEndpoint ep, 
                                                    final MessageExchange exchng) {
        LOG.fine("isExchangeWithConsumerOkay: endpoint: " + ep 
                 + " exchange: " + exchng);
        return true;
    }
    
    public final ServiceEndpoint resolveEndpointReference(final DocumentFragment documentFragment) {
        return null;
    }
    
    
    private void configureJBITransportFactory(DeliveryChannel chnl, CeltixServiceUnitManager mgr)
        throws BusException { 
        getTransportFactory().setDeliveryChannel(chnl);
    }
    
    private void registerJBITransport(Bus argBus, CeltixServiceUnitManager mgr) throws JBIException { 
        try { 
           
            getTransportFactory().init(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
        } catch (Exception ex) {
            throw new JBIException("failed to register JBI transport factory", ex);
        }
    } 
    
    private JBITransportFactory getTransportFactory() throws BusException { 
        assert bus != null;
        
        try { 
            JBITransportFactory transportFactory = 
                (JBITransportFactory)bus.getTransportFactoryManager()
                    .getTransportFactory(JBI_TRANSPORT_ID);
            
            return transportFactory;
        } catch (BusException ex) { 
            LOG.log(Level.SEVERE, "error initializing bus", ex);
            throw ex;
        }
    }
    
    private ComponentClassLoader createClassLoader() throws JBIException { 
        
        try { 
            
            File root = new File(ctx.getInstallRoot());
            File[] jars = root.listFiles(new FilenameFilter() {
                public boolean accept(File f, String name) { 
                    return name.endsWith(".jar");
                }
            });
            
            URL urls[] = new URL[jars.length];
            int i = 0;
            for (File jar : jars) { 
                urls[i] = jar.toURL();
                i++;
            }
            
            return new ComponentClassLoader(urls, getClass().getClassLoader());
        } catch (MalformedURLException ex) { 
            throw new JBIException("failed to construct component classloader", ex);
        } 
    } 
}
