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


package org.apache.cxf.jbi.transport;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jbi.messaging.DeliveryChannel;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.jbi.se.CXFServiceUnitManager;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class JBITransportFactory extends AbstractTransportFactory implements ConduitInitiator,
    DestinationFactory {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JBITransportFactory.class);

    private CXFServiceUnitManager suManager; 
    private DeliveryChannel deliveryChannel;
    private Bus bus;
    
    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    public Bus getBus() {
        return bus;
    }

    public DeliveryChannel getDeliveryChannel() {
        return deliveryChannel;
    }

    public void setDeliveryChannel(DeliveryChannel newDeliverychannel) {
        LOG.fine(new org.apache.cxf.common.i18n.Message(
            "CONFIG.DELIVERY.CHANNEL", LOG).toString() + newDeliverychannel);
        deliveryChannel = newDeliverychannel;
    }

    public CXFServiceUnitManager getServiceUnitManager() { 
        return suManager; 
    }
    
    public void setServiceUnitManager(CXFServiceUnitManager sum) {
        if (sum == null) { 
            Thread.dumpStack(); 
        } 
        LOG.fine(new org.apache.cxf.common.i18n.Message(
            "CONFIG.SU.MANAGER", LOG).toString() + sum);
        suManager = sum;
    }

    public Conduit getConduit(EndpointInfo targetInfo) throws IOException {
        return getConduit(targetInfo, null);
    }

    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        Conduit conduit = new JBIConduit(target, deliveryChannel);
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(conduit);
        }
        return conduit;
    }

    public Destination getDestination(EndpointInfo ei) throws IOException {
        JBIDestination destination = new JBIDestination(this, ei, deliveryChannel, suManager);
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(destination);
        }
        return destination;
    }

}
