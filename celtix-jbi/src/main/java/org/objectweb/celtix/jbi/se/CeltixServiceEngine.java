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
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.transport.JBITransportFactory;


/** A JBI component.  Initializes the Celtix JBI transport
 */
public class CeltixServiceEngine implements ComponentLifeCycle, Component {
    
    public static final String JBI_TRANSPORT_ID = "http://celtix.object.org/transport/jbi";
    private static final String CELTIX_CONFIG_FILE = "celtix-config.xml";
    private static final String PROVIDER_PROP = "javax.xml.ws.spi.Provider";
    
    
    private static final Logger LOG = LogUtils.getL7dLogger(CeltixServiceEngine.class);
    
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
        LOG.info(new Message("SE.SHUTDOWN", LOG).toString());
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
                LOG.info(new Message("SE.SET.CONFIGURATION", LOG) + System.getProperty("celtix.config.file"));
            } else { 
                LOG.severe(new Message("SE.NOT.FOUND.CONFIGURATION", LOG).toString() + metaInfDir);
            } 
            
            ComponentClassLoader loader = createClassLoader();
            
            initializeBus();
            suManager = new CeltixServiceUnitManager(bus, componentContext, loader);
            registerJBITransport(bus, suManager);
            
            LOG.info(new Message("SE.INSTALL.ROOT", LOG) + componentContext.getInstallRoot());
            LOG.info(new Message("SE.INIT.COMPLETE", LOG).toString());
        } catch (Throwable ex) { 
            ex.printStackTrace();
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
            throw new JBIException(ex);
        } 
    }
    
    
    private void initializeBus() throws JBIException { 
        
        try { 
            LOG.info(new Message("SE.INIT.BUS", LOG).toString());
            bus = Bus.init();
            LOG.info(new Message("SE.INIT.BUS.COMPLETE", LOG).toString());
        } catch (Exception ex) { 
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
            throw new JBIException(ex);
        } 
    } 
    
    
    public final void start() throws JBIException {
        
        try { 
            LOG.info(new Message("SE.STARTUP", LOG).toString());
            DeliveryChannel chnl = ctx.getDeliveryChannel();
            configureJBITransportFactory(chnl, suManager); 
            LOG.info(new Message("SE.STARTUP.COMPLETE", LOG).toString());
        } catch (BusException ex) {
            throw new JBIException(ex);
        }
    }
    
    public final void stop() throws JBIException {
        LOG.info(new Message("SE.STOP", LOG).toString());
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
            throw new JBIException(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString(), ex);
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
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
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
            throw new JBIException(new Message("SE.FAILED.CLASSLOADER", LOG).toString(), ex);
        } 
    } 
}
