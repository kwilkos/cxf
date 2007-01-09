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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.generators;

import java.util.Iterator;
import java.util.Map;

import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.common.model.JavaPort;
import org.apache.cxf.tools.common.model.JavaServiceClass;

public class ServerGenerator extends AbstractGenerator {

    private static final String SRV_TEMPLATE = TEMPLATE_BASE + "/server.vm";

    public ServerGenerator() {
        this.name = ToolConstants.SVR_GENERATOR;
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_GEN_SERVER)
            || env.optionSet(ToolConstants.CFG_SERVER)
            || env.optionSet(ToolConstants.CFG_ALL)) {
            return false;
        } 
        if (env.optionSet(ToolConstants.CFG_GEN_ANT)
            || env.optionSet(ToolConstants.CFG_GEN_TYPES)
            || env.optionSet(ToolConstants.CFG_GEN_CLIENT)
            || env.optionSet(ToolConstants.CFG_GEN_IMPL)
            || env.optionSet(ToolConstants.CFG_GEN_SEI)
            || env.optionSet(ToolConstants.CFG_GEN_SERVICE)) {
            return true;
        }
        return true;
    }

    public void generate(ToolContext penv) throws ToolException {
        this.env = penv;
        JavaModel javaModel = env.getJavaModel();

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
            if (penv.optionSet(ToolConstants.CFG_IMPL_CLASS)) {
                setAttributes("impl", 
                              (String)penv.get(ToolConstants.CFG_IMPL_CLASS));
            } else {
                setAttributes("impl", intf.getName() + "Impl");
            }
            setAttributes("address", address);
            setCommonAttributes();

            doWrite(SRV_TEMPLATE, parseOutputName(intf.getPackageName(), serverClassName));
        }
    }

}
