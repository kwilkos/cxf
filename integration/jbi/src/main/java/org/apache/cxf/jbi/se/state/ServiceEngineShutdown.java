/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



package org.apache.cxf.jbi.se.state;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;

//import org.apache.cxf.Bus;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jbi.se.CXFServiceUnitManager;
import org.apache.cxf.jbi.se.ComponentClassLoader;
//import org.apache.cxf.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineShutdown extends AbstractServiceEngineStateMachine {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceEngineShutdown.class);
    
       
    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        LOG.info("in shutdown state");
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
            suManager = new CXFServiceUnitManager(bus, ctx, loader);
            //registerJBITransport(bus, suManager);
            
            LOG.info(new Message("SE.INSTALL.ROOT", LOG) + installRoot);
            LOG.info(new Message("SE.INIT.COMPLETE", LOG).toString());
            
        } catch (Throwable e) {
            throw new JBIException(e);
        }
    }
    
    private void initializeBus() throws JBIException { 
        
        try { 
            LOG.info(new Message("SE.INIT.BUS", LOG).toString());
            //bus = Bus.init();
            LOG.info(new Message("SE.INIT.BUS.COMPLETE", LOG).toString());
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
            if (jars.length == 0) {
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
