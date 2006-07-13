package org.objectweb.celtix.jbi.se.state;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.CeltixServiceUnitManager;
import org.objectweb.celtix.jbi.se.ComponentClassLoader;
import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineShutdown extends AbstractServiceEngineStateMachine {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceEngineShutdown.class);
    
       
    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        LOG.fine("in shutdown state");
        if (operation == SEOperation.init) {
            initSE(context);
            ServiceEngineStateFactory.getInstance().setCurrentState(
                ServiceEngineStateFactory.getInstance().getStopState());
        } else if (operation == SEOperation.shutdown) {
            throw new JBIException("This JBI component is already shutdown");
        } else if (operation == SEOperation.stop) {
            throw new JBIException("This operation is unsupported, cannot stop a shutdown JBI component");
        } else if (operation == SEOperation.start) {
            throw new JBIException("Cannot start a shutdown JBI component directly, need init first");
        }
    }

    private void initSE(ComponentContext context) throws JBIException {
        
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            
            System.setProperty(PROVIDER_PROP, "org.objectweb.celtix.bus.jaxws.spi.ProviderImpl");
            ctx = context;
            if (ctx == null) {
                return;
            }
            String installRoot = ctx.getInstallRoot();
            File metaInfDir = new File(installRoot, "META-INF");
            File celtixConfig = new File(metaInfDir, CELTIX_CONFIG_FILE); 
            
            if (celtixConfig.exists()) { 
                System.setProperty("celtix.config.file", celtixConfig.toURL().toString());
                LOG.info(new Message("SE.SET.CONFIGURATION", LOG) + System.getProperty("celtix.config.file"));
            } else { 
                LOG.severe(new Message("SE.NOT.FOUND.CONFIGURATION", LOG).toString() + metaInfDir);
            } 
            
            ComponentClassLoader loader = createClassLoader();
            
            initializeBus();
            suManager = new CeltixServiceUnitManager(bus, ctx, loader);
            registerJBITransport(bus, suManager);
            
            LOG.fine(new Message("SE.INSTALL.ROOT", LOG) + installRoot);
            LOG.fine(new Message("SE.INIT.COMPLETE", LOG).toString());
            
        } catch (Throwable e) {
            throw new JBIException(e);
        }
    }
    
    private void initializeBus() throws JBIException { 
        
        try { 
            LOG.fine(new Message("SE.INIT.BUS", LOG).toString());
            bus = Bus.init();
            LOG.fine(new Message("SE.INIT.BUS.COMPLETE", LOG).toString());
        } catch (Exception ex) { 
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
            throw new JBIException(ex);
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
            URL[] urls;
            if (jars == null || jars.length == 0) {
                urls = new URL[0];
            } else {
                urls = new URL[jars.length];
                int i = 0;
                for (File jar : jars) { 
                    urls[i] = jar.toURL();
                    i++;
                }
            }
            
            return new ComponentClassLoader(urls, getClass().getClassLoader());
        } catch (MalformedURLException ex) { 
            throw new JBIException(new Message("SE.FAILED.CLASSLOADER", LOG).toString(), ex);
        } 
    } 
    
    
}
