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

package org.apache.cxf.systest.common;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.BusFactoryHelper;
import org.apache.cxf.testutil.common.AbstractClientServerSetupBase;

public abstract class ClientServerSetupBase extends AbstractClientServerSetupBase {
    protected String configFileName;
    private Bus bus; 

    public ClientServerSetupBase(Test arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        if (configFileName != null) {
            System.setProperty("cxf.config.file", configFileName);
        }
        BusFactory bf = BusFactoryHelper.newInstance();
        bus = bf.createBus();
        bf.setDefaultBus(bus);
        super.setUp();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        if (null != bus) {
            bus.shutdown(true);
            bus = null;
        }
        if (configFileName != null) {
            System.clearProperty("cxf.config.file");
        }
    } 
    
    protected Bus getBus() {
        return bus;
    }

    protected void setBus(Bus b) {
        bus = b;
    }
}
