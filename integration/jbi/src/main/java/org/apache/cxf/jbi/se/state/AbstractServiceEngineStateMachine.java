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

import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jbi.se.CXFServiceUnitManager;
import org.apache.cxf.jbi.transport.JBITransportFactory;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;

public abstract class AbstractServiceEngineStateMachine implements ServiceEngineStateMachine {

    static final String CXF_CONFIG_FILE = "cxf.xml";
    static final String PROVIDER_PROP = "javax.xml.ws.spi.Provider";
    static CXFServiceUnitManager suManager;
    static ComponentContext ctx;
    static Bus bus;
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractServiceEngineStateMachine.class);
      
    JBITransportFactory jbiTransportFactory;
   

    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        
    }

    void configureJBITransportFactory(DeliveryChannel chnl, CXFServiceUnitManager mgr)
        throws BusException { 
        getTransportFactory().setDeliveryChannel(chnl);
    }


    
    
    void registerJBITransport(Bus argBus, CXFServiceUnitManager mgr) throws JBIException { 
        try { 
            getTransportFactory().setBus(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
            replaceDestionFactory();
        } catch (Exception ex) {
            LOG.severe(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString());
            throw new JBIException(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString(), ex);
        }
    }
    
    public static CXFServiceUnitManager getSUManager() {
        return suManager;
    }
    
    private void deregisterTransport(String transportId) {
        bus.getExtension(DestinationFactoryManager.class).deregisterDestinationFactory(transportId);        
    }

    /**
     * @return
     */
    protected JBITransportFactory getTransportFactory() {
        assert bus != null;
        if (jbiTransportFactory == null) {
            jbiTransportFactory = new JBITransportFactory();
            jbiTransportFactory.setBus(bus);
            
        }
        return jbiTransportFactory;
    }

    private void registerTransport(DestinationFactory factory, String namespace) {
        bus.getExtension(DestinationFactoryManager.class).registerDestinationFactory(
                                                                  namespace,
                                                                  factory);
    }

    private void replaceDestionFactory() {
        DestinationFactory factory = getTransportFactory();
        

        deregisterTransport("http://schemas.xmlsoap.org/wsdl/soap/http");
        deregisterTransport("http://schemas.xmlsoap.org/soap/http");
        deregisterTransport("http://www.w3.org/2003/05/soap/bindings/HTTP/");
        deregisterTransport("http://schemas.xmlsoap.org/wsdl/http/");
        deregisterTransport("http://cxf.apache.org/transports/http/configuration");
        deregisterTransport("http://cxf.apache.org/bindings/xformat");
        
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/soap/http");
        registerTransport(factory, "http://schemas.xmlsoap.org/soap/http");
        registerTransport(factory, "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        registerTransport(factory, "http://schemas.xmlsoap.org/wsdl/http/");
        registerTransport(factory, "http://cxf.apache.org/transports/http/configuration");
        registerTransport(factory, "http://cxf.apache.org/bindings/xformat");
    }

    
}
