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
package org.apache.cxf.tools.java2wsdl.processor;

import java.util.List;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJavaContainer;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;
import org.apache.cxf.tools.wsdlto.core.FrontEndProfile;
import org.apache.cxf.tools.wsdlto.core.PluginLoader;

public class ServiceInfoToJavaProcessor implements Processor {
    private ToolContext env;
    public void process() {
        env.put(FrontEndProfile.class, PluginLoader.getInstance().getFrontEndProfile("jaxws"));
        env.put(DataBindingProfile.class, PluginLoader.getInstance().getDataBindingProfile("jaxb"));
        
        if (env.optionSet(ToolConstants.CFG_CLIENT)) {
            env.put(ToolConstants.CFG_GEN_SERVICE, ToolConstants.CFG_GEN_SERVICE);
            env.put(ToolConstants.CFG_GEN_CLIENT, ToolConstants.CFG_GEN_CLIENT);
        }
        
        if (env.optionSet(ToolConstants.CFG_SERVER)) {
            env.put(ToolConstants.CFG_GEN_SERVER, ToolConstants.CFG_GEN_SERVER);
            Boolean fromSEI = (Boolean)env.get(ToolConstants.GEN_FROM_SEI);
            if (env.optionSet(ToolConstants.CFG_IMPL) && fromSEI) {
                env.put(ToolConstants.CFG_GEN_IMPL, ToolConstants.CFG_GEN_IMPL);       
            }
        }
        
        List<ServiceInfo> services = (List<ServiceInfo>)env.get(ToolConstants.SERVICE_LIST);
        ServiceInfo serviceInfo = services.get(0);
        if (serviceInfo.getEndpoints().iterator().hasNext()) {
            EndpointInfo endpointInfo = serviceInfo.getEndpoints().iterator().next();
            env.put(ToolConstants.CFG_WSDLURL, endpointInfo.getAddress() + "?wsdl");
        } else {
            env.put(ToolConstants.CFG_WSDLURL, "dummy");
        }
        try {
            WSDLToJavaContainer w2j = new WSDLToJavaContainer("wsdl2java", null);
            w2j.setContext(env);
            w2j.execute();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
           
    }
    
    public void setEnvironment(ToolContext arg) {
        this.env = arg;
    }
    
    

}
