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

package org.apache.cxf.tools.wsdl2java.generator;

import java.util.Iterator;
import java.util.Map;

import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.common.model.JavaPort;
import org.apache.cxf.tools.common.model.JavaServiceClass;

public class ServerGenerator extends AbstractGenerator {

    private static final String SRV_TEMPLATE = TEMPLATE_BASE + "/server.vm";
    
    public ServerGenerator(JavaModel jmodel, ProcessorEnvironment env) {
        super(jmodel, env);
        this.name = ToolConstants.SVR_GENERATOR;
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_SERVER)
                || env.optionSet(ToolConstants.CFG_GEN_SERVER)
                || env.optionSet(ToolConstants.CFG_ALL)) {
            return false;
        }
        return true;
    }

    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Map<String, JavaInterface> interfaces = javaModel.getInterfaces();
        for (Iterator iter = interfaces.keySet().iterator(); iter.hasNext();) {
            String interfaceName = (String)iter.next();
            JavaInterface intf = interfaces.get(interfaceName);
            String address = "";

            Iterator it = javaModel.getServiceClasses().values().iterator();
            while (it.hasNext()) {
                JavaServiceClass js = (JavaServiceClass)it.next();
                Iterator i = js.getPorts().iterator();
                while (i.hasNext()) {
                    JavaPort jp = (JavaPort)i.next();
                    if (interfaceName.equals(jp.getPortType())) {
                        address = jp.getBindingAdress();
                        break;
                    }
                }
                if (!"".equals(address)) {
                    break;
                }
            }
            

            String serverClassName = interfaceName + "Server";
            
            while (isCollision(intf.getPackageName(), serverClassName)) {
                serverClassName = serverClassName + "_Server";
            }
           
            clearAttributes();
            setAttributes("serverClassName", serverClassName);
            setAttributes("intf", intf);
            setAttributes("address", address);
            setCommonAttributes();
                       
            doWrite(SRV_TEMPLATE, parseOutputName(intf.getPackageName(), serverClassName));
        }
    }

}
