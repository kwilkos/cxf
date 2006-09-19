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

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractCommandLineConfiguration implements CommandlineConfiguration {

    Collection<CommandLineOption> options;
    
    // Configuration interface
    
    // methods available to concrete implementations
    
    protected AbstractCommandLineConfiguration() {
        options = new ArrayList<CommandLineOption>();
    }
        
    /* (non-Javadoc)
     * @see org.apache.cxf.configuration.Configuration#getObject(java.lang.String)
     */
    public Object getObject(String name) {
        return getOption(name).getValue();
    }



    /**
     * Parses the arguments and initialises the options. 
     * 
     * @param args the command line arguments
     * @param consume specifies whether the command line options and their
     * arguments should be removed from the command line after processing.
     */
    protected void parseCommandLine(String[] args, boolean consume) {
    }
    
    /**
     * Adds an option to the command line configuration. 
     * Typically invoked in static initializers.
     * 
     * @param option the <code>CommandLineOption</code> to add.
     */
    protected void addOption(CommandLineOption option) {
        options.add(option);
    }
    // private methods from here on
    
    /**
     * Returns the <code>CommandLineOption</code> identified by the name or 
     * null of no such option exists.
     * 
     * @param name identifies the option (shortcuts may be used)
     */
    private CommandLineOption getOption(String name) {
        /*
        for (CommandLineOption o : options) {
        }
        */
        return null;
    }

}
