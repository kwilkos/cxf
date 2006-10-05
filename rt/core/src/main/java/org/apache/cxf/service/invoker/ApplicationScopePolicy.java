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

import org.apache.cxf.common.util.factory.CachingPool;
import org.apache.cxf.common.util.factory.Factory;
import org.apache.cxf.common.util.factory.Pool;
import org.apache.cxf.common.util.factory.PooledFactory;
import org.apache.cxf.message.Exchange;

/**
 * This scope policy implements one servant instance per service.
 * <p>
 * 
 * @author Ben Yu Feb 6, 2006 11:38:08 AM
 */
public class ApplicationScopePolicy implements ScopePolicy {

    private final Pool pool = new CachingPool();

    public Factory applyScope(Factory f, Exchange ex) {
        return new PooledFactory(f, pool);
    }

    public String toString() {
        return "application scope";
    }

    public static ScopePolicy instance() {
        return new ApplicationScopePolicy();
    }
}
