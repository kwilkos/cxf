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

package org.apache.cxf.service.invoker;

import org.apache.cxf.common.util.factory.AbstractPool;
import org.apache.cxf.common.util.factory.Factory;
import org.apache.cxf.common.util.factory.Pool;
import org.apache.cxf.common.util.factory.PooledFactory;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.Service;
import org.apache.cxf.transport.Session;

/**
 * This scope policy implements one servant instance per session.
 * <p>
 * 
 * @author Ben Yu Feb 6, 2006 11:41:08 AM
 */
public class SessionScopePolicy implements ScopePolicy {

    private static SessionScopePolicy singleton = new SessionScopePolicy();

    /**
     * Get the key for caching a service.
     * 
     * @param service the service.
     * @return the key.
     */
    protected Object getServiceKey(Service service) {
        return service.getName();
    }

    public Factory applyScope(Factory f, Exchange ex) {
        Service s = ex.get(Service.class);
        return new PooledFactory(f, getSessionScope(getServiceKey(s), ex.getSession()));
    }

    public String toString() {
        return "session scope";
    }

    private static Pool getSessionScope(final Object key, final Session session) {
        return new AbstractPool() {
            public Object get() {
                return session.get(key);
            }

            public void set(Object val) {
                session.put(key, val);
            }

            public String toString() {
                return "session scope";
            }

            /*
             * This is not guaranteed to be safe with concurrent access to
             * HttpSession. But better than nothing.
             */
            protected Object getMutex() {
                return Service.class;
            }
        };
    }

    public static ScopePolicy instance() {
        return singleton;
    }
}
