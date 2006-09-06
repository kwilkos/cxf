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



public interface ConfigurationProvider {

    /**
     * Lookup the value for the configuration item with the given name in the 
     * underlying store.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    Object getObject(String name);
    
    /**
     * Change the value of  the configuration item with the given name.
     * Return true if the change was accepted and the value changed.
     * It is the providers responsibility to persiste the change in its underlying store
     * if it accepts the change.
     * 
     * @param name the name of the configuration item.
     * @param value the new value for the configuration item.
     * @return true if the change was accepted.
     */
    boolean setObject(String name, Object value);

    /**
     * Save the changes
     * 
     * @return true if the save was successful.
     */
    boolean save();
}
