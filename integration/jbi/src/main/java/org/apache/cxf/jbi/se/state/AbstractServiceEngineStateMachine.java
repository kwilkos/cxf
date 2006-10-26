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

//import java.util.logging.Level;
//import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
//import org.apache.cxf.common.i18n.Message;
//import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jbi.se.CXFServiceUnitManager;
//import org.apache.cxf.jbi.transport.JBITransportFactory;

public abstract class AbstractServiceEngineStateMachine implements ServiceEngineStateMachine {

    static final String CELTIX_CONFIG_FILE = "celtix-config.xml";
    static final String PROVIDER_PROP = "javax.xml.ws.spi.Provider";
    static CXFServiceUnitManager suManager;
    static ComponentContext ctx;
    static Bus bus;
    //private static final Logger LOG = LogUtils.getL7dLogger(AbstractServiceEngineStateMachine.class);
    //private static final String JBI_TRANSPORT_ID = "http://celtix.object.org/transport/jbi";
      
   

    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        
    }

    void configureJBITransportFactory(DeliveryChannel chnl, CXFServiceUnitManager mgr)
        throws BusException { 
        //getTransportFactory().setDeliveryChannel(chnl);
    }


    /*JBITransportFactory getTransportFactory() throws BusException { 
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
    
    void registerJBITransport(Bus argBus, CeltixServiceUnitManager mgr) throws JBIException { 
        try { 
           
            getTransportFactory().init(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
        } catch (Exception ex) {
            throw new JBIException(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString(), ex);
        }
    }*/ 
    
    public static CXFServiceUnitManager getSUManager() {
        return suManager;
    }
    
}
