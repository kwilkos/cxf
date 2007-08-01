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

package org.apache.cxf.common.util.factory;

/**
 * This class decorates a Factory object that uses a Pool strategy to cache the
 * factory result;
 * <p>
 * 
 * @author Ben Yu Feb 2, 2006 11:57:12 AM
 */
public class PooledFactory implements Factory {
    private final Factory factory;

    private final Pool pool;

    public PooledFactory(Factory factory, Pool pool) {
        this.factory = factory;
        this.pool = pool;
    }
    
    public Object create() throws Throwable {
        return pool.getInstance(factory);
    }

}
