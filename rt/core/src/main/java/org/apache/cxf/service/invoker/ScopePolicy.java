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

import org.apache.cxf.common.util.factory.Factory;
import org.apache.cxf.message.Exchange;

/**
 * This interface represents a scoping policy that caches servant instances
 * created by a Factory.
 * <p>
 * 
 * @author Ben Yu Feb 6, 2006 12:47:38 PM
 */
public interface ScopePolicy {
    /**
     * Apply scope policy to a Factory object so that the instance created by
     * the Factory object can be cached properly.
     * 
     * @param f the Factory object.
     * @param exchange The message exchange which the scope is applied to.
     * @return the Factory object that honors the scope.
     */
    Factory applyScope(Factory f, Exchange exchange);
}
