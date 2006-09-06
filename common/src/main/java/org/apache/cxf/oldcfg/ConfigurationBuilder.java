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

package org.apache.cxf.oldcfg;

import java.net.URL;


public interface ConfigurationBuilder {
    
    /**
     * Returns the configurations builders's url which it provides to the <code>configuration</code> 
     * objects it creates and their providers.
     * 
     * @return the builder's url
     */
    URL getURL();
    
    /**
     * Returns the <code>Configuration</code> object with the specified namespace and
     * identifer, creating it if necessary.
     * @throws ConfigurationException if no configuration metadata for the specified namespace is 
     * available.
     * @param namespaceUri the configuration namespace.
     * @param id the configuration identifier.
     * @return the configuration.
     */
    Configuration getConfiguration(String namespaceUri, CompoundName id);
    
    /**
     * Returns the configuration metadata model for the given namespace or null if no such
     * model is stored in this builder.
     * @param namespaceURI the configuration namespace.
     * @return the configuration metadata model.
     */
    ConfigurationMetadata getModel(String namespaceURI);
    
    void addModel(String namespaceURI, ConfigurationMetadata model);
     
}
