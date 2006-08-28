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

package org.apache.cxf.bus.resource;

import java.util.Map;

import org.apache.cxf.BusException;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.PropertiesResolver;


public class ResourceManagerImpl extends DefaultResourceManager {

    public ResourceManagerImpl() {
        super();
    }
    
    public ResourceManagerImpl(Map<String, Object> properties) throws BusException { 
        super();
        registeredResolvers.clear();
        
        registeredResolvers.add(new PropertiesResolver(properties));
        
        // TODO: replace by dynamic loading
        
        /*
        Configuration conf = bus.getConfiguration(); g
        assert null != conf;
        Object obj = conf.getObject("resourceResolvers");
        assert null != obj;
        
        
        
        try { 
            for (String className : ((StringListType)obj).getItem()) { 
                if (LOG.isLoggable(Level.FINEST)) { 
                    LOG.finest("attempting to load resolver " + className);
                }
                
                Class<? extends ResourceResolver> clz = getClass().getClassLoader().loadClass(className)
                    .asSubclass(ResourceResolver.class);

                ResourceResolver rr = clz.newInstance();
                registeredResolvers.add(rr);
            } 
        } catch (Exception ex) { 
            throw new BusException(ex);
        } 
        */
    } 
    
}
