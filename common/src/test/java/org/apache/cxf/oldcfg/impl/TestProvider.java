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

import java.net.URL;

import javax.annotation.Resource;

import org.apache.cxf.oldcfg.Configuration;
import org.apache.cxf.oldcfg.ConfigurationProvider;

public class TestProvider implements ConfigurationProvider {

    @Resource()
    URL url;
    
    @Resource()
    Configuration configuration;
    
    @Resource()
    String name;
    
    public Object getObject(String n) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean setObject(String n, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean save() {
        return false;
    }
}
