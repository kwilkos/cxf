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

package org.apache.cxf.management;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.management.MBeanServer;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.event.ComponentEventFilter;
import org.apache.cxf.event.Event;
import org.apache.cxf.event.EventListener;
import org.apache.cxf.event.EventProcessor;
import org.apache.cxf.management.jmx.JMXManagedComponentManager;




public class InstrumentationManagerImpl extends InstrumentationManagerConfigBean
    implements InstrumentationManager, EventListener {
    static final Logger LOG = LogUtils.getL7dLogger(InstrumentationManagerImpl.class);

    Bus bus;
    
    private List <Instrumentation> instrumentations;
    private JMXManagedComponentManager jmxManagedComponentManager;
    
   
    public InstrumentationManagerImpl() {
        if (null == getInstrumentation()) {
            setInstrumentation(new InstrumentationType());
        }      
        if (null == getJMXConnectorPolicy()) {
            setJMXConnectorPolicy(new JMXConnectorPolicyType());
        }      
    }
    
    public Bus getBus() {
        return bus;
    }
    
    @Resource
    public void setBus(Bus bus) {        
        this.bus = bus;
        initInstrumentationManagerImpl();
    }
    
    public void initInstrumentationManagerImpl() {
        LOG.info("Setting up InstrumentationManager");
        
        if (getInstrumentation().isEnabled()) {
            instrumentations = new LinkedList<Instrumentation>();
            //regist to the event process
            ComponentEventFilter componentEventFilter = new ComponentEventFilter();
            EventProcessor ep = bus.getExtension(EventProcessor.class);
            if (null != ep) {                
                ep.addEventListener((EventListener)this, componentEventFilter);
            }    
        }
            
        if (getInstrumentation().isJMXEnabled()) {           
            jmxManagedComponentManager = new JMXManagedComponentManager();
            jmxManagedComponentManager.init(getJMXConnectorPolicy());
        }
    }
    
    public void shutdown() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Shutdown InstrumentationManager ");
        }
        
        if (jmxManagedComponentManager != null) {
            jmxManagedComponentManager.shutdown();
        }
    }
    
    public void register(Instrumentation it) {
        if (it == null) {
            // just return
            return;
        } else {
            instrumentations.add(it); 
            if (jmxManagedComponentManager != null) {
                jmxManagedComponentManager.registerMBean(it);
            }
        }        
    }

    public void unregister(Object component) {
        for (Iterator<Instrumentation> i = instrumentations.iterator(); i.hasNext();) {
            Instrumentation it = i.next();
            if (it.getComponent() == component) {
                i.remove();   
                if (it != null && jmxManagedComponentManager != null) {
                    jmxManagedComponentManager.unregisterMBean(it);               
                }
                return;
            }
        }
    }
    
    public List<Instrumentation> getAllInstrumentation() {
        // TODO need to add more query interface
        return instrumentations;
    }

    public MBeanServer getMBeanServer() {        
        return jmxManagedComponentManager.getMBeanServer();
    }

    public void processEvent(Event e) {
        Instrumentation it;
        if (e.getID().getLocalPart().equals(ComponentEventFilter.COMPONENT_CREATED_EVENT)) {
            it = ((InstrumentationFactory)(e.getSource())).createInstrumentation();
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Instrumentation register " + e.getSource().getClass().getName());
            }   
            register(it);          
            
        } else if (e.getID().getLocalPart().equals(ComponentEventFilter.COMPONENT_REMOVED_EVENT)) {           
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Instrumentation unregister " + e.getSource().getClass().getName());
            }    
            unregister(e.getSource());
        }
        
    }
      

}
