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

/**
 * Represents a command line option, similar to <code>ConfigurationItem</code>.
 * 
 * @author asmyth
 *
 */
public class CommandLineOption {
    
    private String name;
    private Object value;
        
    public CommandLineOption(String optionName) {
        name = optionName;
    }
    
    public String toString() {
        return name;        
    }
    
    
    public String getName() {
        return name;
    }
    
    public String getShortcut() {
        return null;
    }
    
    public Object getValue() {
        return value;
    }
    
    public boolean exists() {
        return false;
    }
    
    public void initialize(String v) {
        value = v;
    }
    
    public void initialize(String[]args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].compareTo(name) == 0 && i < args.length - 1) {
                    value = args[i + 1];
                    break;
                }
            }
        }
    }
}
