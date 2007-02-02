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

package org.apache.cxf.extension;

import org.apache.cxf.Bus;



/**
 * Helper class to be used as constructor argument for classes that want to be
 * registered as bus extensions. It avoids a @Resource annotated data member of type Bus
 * in the extension class and a @PostConstruct annotated method in which the extension
 * registration takes place.
 */
public class BusExtensionRegistrar {
   
    private Bus bus;    
    
    public void setBus(Bus b) {
        bus = b;
    }

    public Bus getBus() {
        return bus;
    }

    public <T> void registerExtension(T extension, Class<T> extensionType) {
        if (null != bus) {
            bus.setExtension(extension, extensionType);
        }
    }
}
