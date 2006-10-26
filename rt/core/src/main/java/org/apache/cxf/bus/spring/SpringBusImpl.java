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

package org.apache.cxf.bus.spring;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.BusState;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;

public class SpringBusImpl extends AbstractBasicInterceptorProvider implements Bus {
    
    private Map<Class, Object> extensions;
    private BusLifeCycleManager lifeCycleManager;
    private String id;
    private BusState state;      
    
    public SpringBusImpl() {
        extensions = new HashMap<Class, Object>();
        state = BusState.INITIAL;
        lifeCycleManager = this.getExtension(BusLifeCycleManager.class);
        if (null != lifeCycleManager) {
            lifeCycleManager.initComplete();
        }
    }
    
    public void setExtensions(Map<Class, Object> e) {
        extensions = e;
    }
    
    public void setId(String i) {
        id = i;
    }

    public final <T> T getExtension(Class<T> extensionType) {
        Object obj = extensions.get(extensionType);
        if (null != obj) {
            return extensionType.cast(obj);
        }
        return null;
    }
    
    public <T> void setExtension(T extension, Class<T> extensionType) {
        extensions.put(extensionType, extension);
    }
     
    public String getId() {
        return id;
    }

    public void run() {
        synchronized (this) {
            if (state == BusState.RUNNING) {
                // REVISIT
                return;
            }
            state = BusState.RUNNING;

            while (state == BusState.RUNNING) {

                try {
                    wait();
                } catch (InterruptedException ex) {
                    // ignore;
                }
            }
        }
    }

    public void shutdown(boolean wait) {
        lifeCycleManager = this.getExtension(BusLifeCycleManager.class);
        if (null != lifeCycleManager) {
            lifeCycleManager.preShutdown();
        }
        synchronized (this) {
            state = BusState.SHUTDOWN;
            notifyAll();
        }
        if (null != lifeCycleManager) {
            lifeCycleManager.postShutdown();
        }
    }
    
    protected BusState getState() {
        return state;
    }

}
