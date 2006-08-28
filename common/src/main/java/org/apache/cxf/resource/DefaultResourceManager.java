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

package org.apache.cxf.resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;

public class DefaultResourceManager implements ResourceManager {
    
    private static final Logger LOG = LogUtils.getL7dLogger(DefaultResourceManager.class);
    private static ResourceManager instance; 

    protected final List<ResourceResolver> registeredResolvers = new LinkedList<ResourceResolver>();

    public DefaultResourceManager() { 
        initializeDefaultResolvers(); 
    } 
    
    public DefaultResourceManager(ResourceResolver resolver) {
        addResourceResolver(resolver);
    }
    
    public DefaultResourceManager(List<ResourceResolver> resolvers) {
        for (Iterator<ResourceResolver> it = resolvers.iterator(); it.hasNext();) {
            addResourceResolver(it.next());
        }
    }
 
    public final <T> T resolveResource(String name, Class<T> type) { 
        return findResource(name, type, false, registeredResolvers);
    } 

    public final <T> T resolveResource(String name, Class<T> type, List<ResourceResolver> resolvers) { 
        return findResource(name, type, false, resolvers);
    } 

    
    public final InputStream getResourceAsStream(String name) { 
        return findResource(name, InputStream.class, true, registeredResolvers);
    } 


    public final void addResourceResolver(ResourceResolver resolver) { 
        if (!registeredResolvers.contains(resolver)) { 
            registeredResolvers.add(0, resolver);
        }
    } 

    public final void removeResourceResolver(ResourceResolver resolver) { 
        if (registeredResolvers.contains(resolver)) { 
            registeredResolvers.remove(resolver);
        }
    } 


    public final List<ResourceResolver> getResourceResolvers() {
        List<ResourceResolver> ret = new ArrayList<ResourceResolver>();
        ret.addAll(registeredResolvers);
        return ret; 
    }

    
    private <T> T findResource(String name, Class<T> type, boolean asStream, 
                               List<ResourceResolver> resolvers) {
        
        if (LOG.isLoggable(Level.FINE)) { 
            LOG.fine("resolving resource <" + name + ">" + (asStream ? " as stream "  
                                                            : " type <" + type + ">"));
        }

        T ret = null; 
        
        for (ResourceResolver rr : resolvers) { 
            if (asStream) { 
                ret = type.cast(rr.getAsStream(name));
            } else { 
                ret = rr.resolve(name, type);
            }
            if (ret != null) { 
                break;
            }
        } 
        return ret;
    } 


    public static synchronized ResourceManager instance() { 
        if (instance == null) { 
            instance = new DefaultResourceManager();
        } 
        return instance;
    }
    public static synchronized void clearInstance() {
        instance = null;
    }

    private void initializeDefaultResolvers() { 
        addResourceResolver(new ClasspathResolver());
        addResourceResolver(new ClassLoaderResolver(getClass().getClassLoader()));
    } 

}
