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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.oldcfg.ConfigurationItemMetadata;
import org.apache.cxf.oldcfg.ConfigurationMetadata;

public class ConfigurationMetadataImpl implements ConfigurationMetadata {

    private final Map<String, ConfigurationItemMetadata> definitions;
    private String namespaceURI;
    private String parentNamespaceURI;

    public ConfigurationMetadataImpl() {
        definitions = new HashMap<String, ConfigurationItemMetadata>();
    }

    protected void addItem(ConfigurationItemMetadata item) {
        definitions.put(item.getName(), item);
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getParentNamespaceURI() {
        return parentNamespaceURI;
    }

    public ConfigurationItemMetadata getDefinition(String name) {
        return definitions.get(name);
    }

    public Collection<ConfigurationItemMetadata> getDefinitions() {
        return definitions.values();
    }

    protected void setNamespaceURI(String uri) {
        namespaceURI = uri;
    }

    protected void setParentNamespaceURI(String uri) {
        parentNamespaceURI = uri;
    }
}
