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

package org.apache.cxf.tools.wsdl2java.processor.compiler;

import java.io.File;
import java.io.IOException;

public class Compiler {
    public boolean internalCompile(String[] args) {

        Process p = null;
        try {

            for (int i = 0; i < args.length; i++) {
                if (!"/".equals(File.separator) && args[i].indexOf("package-info") == -1) {
                    args[i] = args[i].replace(File.separatorChar, '/');
                }              
            }

            p = Runtime.getRuntime().exec(args);
                  
            if (p.getErrorStream() != null) {
                StreamPrinter errorStreamPrinter = 
                    new StreamPrinter(p.getErrorStream(), "", System.out);
                errorStreamPrinter.run();
            }
            
            
            if (p.getInputStream() != null) {
                StreamPrinter infoStreamPrinter = new StreamPrinter(p.getInputStream(), "[INFO]", System.out);
                infoStreamPrinter.run();
            }
            
            

            if (p != null) {
                return p.waitFor() == 0 ? true : false;
            }
        } catch (SecurityException e) {
            System.err.println("[ERROR] SecurityException during exec() of compiler \"" + args[0] + "\".");
        } catch (InterruptedException e) {
            // ignore

        } catch (IOException e) {
            System.err.print("[ERROR] IOException during exec() of compiler \"" + args[0] + "\"");
            System.err.println(". Check your path environment variable.");
        }

        return false;
    }
}
