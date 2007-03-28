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

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaModel;

public class AntGenerator extends AbstractJAXWSGenerator {

    private static final String ANT_TEMPLATE = TEMPLATE_BASE + "/build.vm";

    public AntGenerator() {
        this.name = ToolConstants.ANT_GENERATOR;
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_ANT)
                || env.optionSet(ToolConstants.CFG_ALL)
                || env.optionSet(ToolConstants.CFG_GEN_ANT)) {
            return false;
        }
        return true;
    }

    public void generate(ToolContext penv) throws ToolException {       
        this.env = penv;
        JavaModel javaModel = env.get(JavaModel.class);
        
        if (passthrough()) {
            return;
        }
        
        if (javaModel.getServiceClasses().size() == 0) {
            ServiceInfo serviceInfo = (ServiceInfo)env.get(ServiceInfo.class);
            String wsdl = serviceInfo.getDescription().getBaseURI();
            Message msg = new Message("CAN_NOT_GEN_ANT", LOG, wsdl);
            if (penv.isVerbose()) {
                System.out.println(msg.toString());
            }
            return;
        }

        clearAttributes();
        setAttributes("intfs", javaModel.getInterfaces().values());
        setAttributes("wsdlLocation", javaModel.getLocation());
        setCommonAttributes();

        doWrite(ANT_TEMPLATE, parseOutputName(null, "build", ".xml"));
    }
}
