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
 * A thread-unsafe implementation of Pool that does simple caching.
 * <p>
 * 
 * @author Ben Yu Feb 2, 2006 12:13:08 PM
 */
public class CachingPool implements Pool {
    private transient Object v;

    private transient boolean pooled;

    private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, java.io.IOException {
        in.defaultReadObject();
        this.pooled = false;
    }

    public Object getInstance(Factory factory) throws Throwable {
        if (!pooled) {
            v = factory.create();
            pooled = true;
        }
        return v;
    }

    public Object getPooledInstance(Object def) {
        return pooled ? v : def;
    }

    /**
     * Is this pool currently having something in cache?
     */
    public boolean isPooled() {
        return pooled;
    }
}
