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

package org.apache.cxf.testutil.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import junit.extensions.TestSetup;
import junit.framework.Test;

public abstract class AbstractClientServerSetupBase extends TestSetup {
    private final List<ServerLauncher> launchers = new ArrayList<ServerLauncher>();  

    public AbstractClientServerSetupBase(Test arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        startServers();
    }
    
    public abstract void startServers() throws Exception;
    
    public void tearDown() throws Exception {
        boolean serverPassed = stopAllServers();
        launchers.clear();
        System.gc();
        assertTrue("server failed", serverPassed);
    } 
    
    protected boolean stopAllServers() {
        boolean passed = true;
        for (ServerLauncher sl : launchers) {
            try { 
                sl.signalStop();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for (ServerLauncher sl : launchers) {
            try { 
                passed = passed && sl.stopServer(); 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        launchers.clear();
        return passed;
    }
    
    public boolean launchServer(Class<?> clz) {
        boolean ok = false;
        try { 
            ServerLauncher sl = new ServerLauncher(clz.getName());
            ok = sl.launchServer();
            assertTrue("server failed to launch", ok);
            launchers.add(sl);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("failed to launch server " + clz);
        }
        
        return ok;
    }
    public boolean launchServer(Class<?> clz, boolean inProcess) {
        boolean ok = false;
        try { 
            ServerLauncher sl = new ServerLauncher(clz.getName(), inProcess);
            ok = sl.launchServer();
            assertTrue("server failed to launch", ok);
            launchers.add(sl);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("failed to launch server " + clz);
        }
        
        return ok;
    }
    public boolean launchServer(Class<?> clz, Map<String, String> props, String[] args) {
        return launchServer(clz, props, args, false);
    }
    public boolean launchServer(Class<?> clz, Map<String, String> props, String[] args,
                                boolean inProcess) {
        boolean ok = false;
        try { 
            ServerLauncher sl = new ServerLauncher(clz.getName(), props, args, inProcess);
            ok = sl.launchServer();
            assertTrue("server failed to launch", ok);
            launchers.add(sl);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("failed to launch server " + clz);
        }
        
        return ok;
    }
}
