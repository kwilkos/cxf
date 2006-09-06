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

package org.apache.cxf.oldcfg.impl;

import javax.xml.namespace.QName;

import org.apache.cxf.oldcfg.ConfigurationItemMetadata;

public class ConfigurationItemMetadataImpl implements ConfigurationItemMetadata {
    
    private String name;
    private LifecyclePolicy lifecyclePolicy = LifecyclePolicy.STATIC;
    private QName type;
    private Object defaultValue;
    
    public String getName() {
        return name;
    }
    
    public QName getType() {
        return type;
    }
        
    public LifecyclePolicy getLifecyclePolicy() {
        return lifecyclePolicy;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    protected void setName(String n) {
        name = n;
    }
    
    protected void setType(QName t) {
        type = t;
    }
    
    protected void setLifecyclePolicy(LifecyclePolicy policy) {
        lifecyclePolicy = policy;
    }
        
    protected void setDefaultValue(Object v) {
        defaultValue = v;
    }
}
