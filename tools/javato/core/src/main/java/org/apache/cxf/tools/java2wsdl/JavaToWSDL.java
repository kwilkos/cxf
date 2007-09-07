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
package org.apache.cxf.tools.java2wsdl;

import java.util.Arrays;
import java.util.List;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.common.toolspec.ToolRunner;

public class JavaToWSDL {

    private String[] args;

    public JavaToWSDL() {
        args = new String[0];
    }

    public JavaToWSDL(String pargs[]) {
        args = pargs;
    }

    public static void main(String[] args) {
        JavaToWSDL j2w = new JavaToWSDL(args);
        try {
            j2w.run();
        } catch (Exception ex) {
            System.err.println("JavaToWSDL Error : " + ex.getMessage());
            System.err.println();
            if (j2w.isVerbose()) {
                ex.printStackTrace();
            }
            if (j2w.isExitOnFinish()) {
                System.exit(1);
            }
        }
    }
    
    private boolean isVerbose() {
        return isSet(new String[]{"-V", "-verbose"});
    }
    
    private boolean isSet(String[] keys) {
        if (args == null) {
            return false;
        }
        List<String> pargs = Arrays.asList(args);
        
        for (String key : keys) {
            if (pargs.contains(key)) {
                return true;
            }
        }
        return false;
    }
    
    private void run() throws Exception {
        ToolRunner.runTool(JavaToWSDLContainer.class, JavaToWSDLContainer.class
                           .getResourceAsStream("java2wsdl.xml"), false, args);
    }
    
    
    private boolean isExitOnFinish() {
        String exit = System.getProperty("exitOnFinish");
        if (StringUtils.isEmpty(exit)) {
            return false;
        }
        return "YES".equalsIgnoreCase(exit) || "TRUE".equalsIgnoreCase(exit);
    }


}
    

