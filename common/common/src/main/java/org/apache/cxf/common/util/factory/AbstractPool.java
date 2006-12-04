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
 * A simple implementation of Pool that uses null to indicate non-existent pool
 * entry.
 * <p>
 * This implementation synchronizes on {@link #getMutex()} for thread safety.
 * </p>
 * 
 * @author Ben Yu Feb 2, 2006 3:14:45 PM
 */
public abstract class AbstractPool implements Pool {
    public Object getInstance(Factory factory) throws Throwable {
        synchronized (getMutex()) {
            Object ret = get();
            if (ret == null) {
                ret = factory.create();
                set(ret);
            }
            return ret;
        }
    }

    public Object getPooledInstance(Object def) {
        synchronized (getMutex()) {
            return ifnull(get(), def);
        }
    }

    public boolean isPooled() {
        synchronized (getMutex()) {
            return get() != null;
        }
    }

    protected static Object ifnull(Object obj, Object def) {
        return obj == null ? def : obj;
    }

    /**
     * Get the pooled instance. null if not found.
     * 
     * @return the pooled instance.
     */
    public abstract Object get();

    /**
     * set an value to the pool.
     * 
     * @param val the value to be pooled.
     */
    public abstract void set(Object val);

    /**
     * Get the object that can be used to synchronize.
     */
    protected abstract Object getMutex();
}
