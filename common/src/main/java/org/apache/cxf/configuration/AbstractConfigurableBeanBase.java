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

package org.apache.cxf.configuration;

import java.util.List;

public abstract class AbstractConfigurableBeanBase implements Configurable {
    
    private List<ConfigurationProvider> providers;
    
    public String getBeanName() {
        return this.getClass().getName();
    }

    public List<ConfigurationProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<ConfigurationProvider> providers) {
        this.providers = providers;
    }
    
    
    protected <T> T tryProviders(Class<T> cls, String name) {
        if (null != providers) {
            for (ConfigurationProvider p : providers) {
                Object value = p.getObject(name);
                if (null != value) {
                    return cls.cast(value);
                }
            }
        }
        return null;
    }
    
    
    
}
