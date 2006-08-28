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

import java.util.Collection;

/**
 * Container for a component's runtime configuration metata.
 *
 */
public interface ConfigurationMetadata {

    /**
     * Gets the namespace URI for this configuration metadata model.
     *
     * @return the configuration metadata model's namespace URI.
     */
    String  getNamespaceURI();

    /**
     * Gets the parent namespace URI for this configuration metadata model.
     * The presence of this value means that the underlying type of
     * configuration can only be created as a child of a configuration with
     * namespace 'parentNamespace'. The absence of this attribute means that it
     * can be be created as a top level configuration object
     *
     * @return the configuration metadata model's parent namespace URI.
     */
    String  getParentNamespaceURI();

    /**
     * Gets the metadata for the specified configuration item name.
     *
     * @param name the name of the configuration item.
     * @return the item's configuration metadata or null if no such item is
     * defined.
     */
    ConfigurationItemMetadata getDefinition(String name);

    /**
     * Gets all configuration metadata items in this container.
     *
     * @return the collection of configuration metadata items.
     */
    Collection<ConfigurationItemMetadata> getDefinitions();
}
