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

package org.apache.cxf.tools.wsdl2java.generator.jaxws;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.common.model.JavaServiceClass;
import org.apache.cxf.tools.util.ProcessorUtil;

public class ServiceGenerator extends AbstractGenerator {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractGenerator.class);
    private static final String SERVICE_TEMPLATE = TEMPLATE_BASE + "/service.vm";

    public ServiceGenerator() {
        this.name = ToolConstants.SERVICE_GENERATOR;
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_GEN_SERVER)) {
            return true;
        }
        return false;
    }

    public void generate(ProcessorEnvironment penv) throws ToolException {
        this.env = penv;
        JavaModel javaModel = env.getJavaModel();

        if (passthrough()) {
            return;
        }

        Map<String, JavaServiceClass> serviceClasses = javaModel.getServiceClasses();

        Iterator ite = serviceClasses.values().iterator();

        while (ite.hasNext()) {

            JavaServiceClass js = (JavaServiceClass)ite.next();

            String location = (String)env.get(ToolConstants.CFG_WSDLURL);
            URL url = null;
            try {
                url = ProcessorUtil.getWSDLURL(location);
            } catch (Exception e) {
                Message message = new Message("FAIL_TO_GET_WSDL", LOG, location);
                throw new ToolException(message, e);
            }

            clearAttributes();

            setAttributes("service", js);
            setAttributes("wsdlLocation", url.toString());
            setCommonAttributes();

            doWrite(SERVICE_TEMPLATE, parseOutputName(js.getPackageName(), js.getName()));
        }
    }
}
