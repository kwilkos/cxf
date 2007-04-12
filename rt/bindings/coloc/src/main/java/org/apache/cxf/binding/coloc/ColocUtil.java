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
package org.apache.cxf.binding.coloc;

import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;

public final class ColocUtil {
    //private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ColocInInterceptor.class);
    private static final Logger LOG = Logger.getLogger(ColocUtil.class.getName());

    private ColocUtil() {
        //Completge
    }

    public static void setPhases(List<Phase> list, String start, String end) {
        Phase startPhase = new Phase(start, 1);
        Phase endPhase = new Phase(end, 2);
        ListIterator<Phase> iter = list.listIterator();
        boolean remove = true;
        while (iter.hasNext()) {
            Phase p = iter.next();
            if (remove 
                && p.getName().equals(startPhase.getName())) {
                remove = false;
            } else if (p.getName().equals(endPhase.getName())) {
                remove = true;
            } else if (remove) {
                iter.remove();
            }
        }
    }
    
    public static InterceptorChain getOutInterceptorChain(Exchange ex, List<Phase> phases) {
        Bus bus = ex.get(Bus.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(phases);
        
        Endpoint ep = ex.get(Endpoint.class);
        List<Interceptor> il = ep.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by endpoint: " + il);
        }
        chain.add(il);
        il = ep.getService().getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by service: " + il);
        }
        chain.add(il);
        il = bus.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);

        return chain;
    }
    
    public static InterceptorChain getInInterceptorChain(Exchange ex, List<Phase> phases) {
        Bus bus = ex.get(Bus.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(phases);
        
        Endpoint ep = ex.get(Endpoint.class);
        List<Interceptor> il = ep.getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by endpoint: " + il);
        }
        chain.add(il);
        il = ep.getService().getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by service: " + il);
        }
        chain.add(il);
        il = bus.getInInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);
        chain.setFaultObserver(new ColocOutFaultObserver(bus));

        return chain;
    }    
    
}
