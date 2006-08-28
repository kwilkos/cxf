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

import javax.xml.namespace.QName;

/**
 * Interface to access configuration metadata accessed at runtime.
 * Acts as a restricted view on configuration metadata, e.g. does 
 * not provide access to a configuration item's documentation. 
 */
public interface ConfigurationItemMetadata {
    
    public enum LifecyclePolicy {
        STATIC,
        PROCESS,
        BUS,
        DYNAMIC
    };
    
    /**
     * Returns the name of this configuration metadata item which must be unique within 
     * its <code>ConfigurationMetadata</code> container.
     * 
     * @return the name of the configuration item.
     */
    String getName();
    
    /** 
     * Returns the type of this configuration metadata item as a <code>QName</code>.
     * The namespaceURI of this <code>QName</code> identifies the XML schema containing the 
     * definition of the (complex or simple) type of this item. The local part 
     * identifies a global element in that schema that is of the underlying type.
     * 
     * @return the type of this configuration metadata item.
     */
    QName getType();

    /**
     * Returns the lifecycle policy for this configuration metadata item. Depending on this value,
     * concrete instances of this configuration metadata item (configuration items) will have values that
     * can never change, can be set once pre process/bus or can be modified ar any time. 
     * 
     * @return the lifecycle policy of this configuration metadata item.
     */
    LifecyclePolicy getLifecyclePolicy();
    
    /** 
     * Returns the default value of this configuration metadata item. The runtime class of this value 
     * depends on the jaxb schema binding for the type of this item. For primitive data types it 
     * is a holder class, e.g. java.lang.Boolean.
     * 
     * @return the default value of this configuration metadata item.
     */
    Object getDefaultValue();
    
}
