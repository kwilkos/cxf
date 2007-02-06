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

import java.io.*;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLVersion;
import org.apache.cxf.tools.java2wsdl.generator.AbstractGenerator;
import org.apache.cxf.tools.java2wsdl.generator.WSDLGeneratorFactory;
import org.apache.cxf.tools.java2wsdl.processor.internal.ServiceBuilder;
import org.apache.cxf.tools.java2wsdl.processor.internal.ServiceBuilderFactory;
import org.apache.cxf.tools.util.AnnotationUtil;

public class JavaToProcessor implements Processor {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToProcessor.class);
    private static final String DEFAULT_BINDING_ID = "http://schemas.xmlsoap.org/soap/http";
    private static final String DEFAULT_ADDRESS = "http://localhost:9090/hello";
    private ToolContext context;

    public void process() throws ToolException {
        init();
        ServiceBuilderFactory builderFactory = ServiceBuilderFactory.getInstance();
        builderFactory.setServiceClass(getServiceClass());
        // TODO check if user specify the style from cli arguments
        //      builderFactory.setStyle(style/from/command/line);
        ServiceBuilder builder = builderFactory.newBuilder();
        builder.setAddress(DEFAULT_ADDRESS);
        builder.setTransportId(DEFAULT_BINDING_ID);
        builder.setBus(getBus());

        ServiceInfo service = builder.build();

        File output = getOutputFile(builder.getOutputFile(),
                                    new File(service.getName().getLocalPart() + ".wsdl"));

        WSDLGeneratorFactory factory = WSDLGeneratorFactory.getInstance();
        factory.setWSDLVersion(getWSDLVersion());

        AbstractGenerator generator = factory.newGenerator();
        generator.setServiceModel(service);
        generator.generate(output);
    }

    protected File getOutputFile(File nameFromClz, File defaultOutputFile) {
        File result = defaultOutputFile;
        String output = (String) context.get(ToolConstants.CFG_OUTPUTFILE);
        if (output != null) {
            result = new File(output);
        }
        if (nameFromClz != null) {
            result = nameFromClz;
        }
        
        // rename the exising wsdl file
        if (result.exists()
            && !result.renameTo(new File(result.getParent(), result.getName()))) {
            throw new ToolException(new Message("OUTFILE_EXISTS", LOG));
        }
        return result;
    }

    public Class getServiceClass() {
        return AnnotationUtil.loadClass((String)context.get(ToolConstants.CFG_CLASSNAME),
                                        getClass().getClassLoader());
    }

    public ToolConstants.WSDLVersion getWSDLVersion() {
        String version = (String) context.get(ToolConstants.CFG_WSDL_VERSION);
        ToolConstants.WSDLVersion wsVersion = WSDLVersion.getVersion(version);
        if (wsVersion == ToolConstants.WSDLVersion.UNKNOWN) {
            wsVersion = ToolConstants.WSDLVersion.WSDL11;
        }
        return wsVersion;
    }

    public Bus getBus() {
        return BusFactory.newInstance().getDefaultBus();
    }

    public void setEnvironment(ToolContext env) {
        this.context = env;
    }

    public ToolContext getEnvironment() {
        return this.context;
    }

    protected void init() {
        if (context.get(ToolConstants.CFG_CLASSPATH) != null) {
            String newCp = (String)context.get(ToolConstants.CFG_CLASSPATH);
            String classpath = System.getProperty("java.class.path");
            System.setProperty("java.class.path", newCp + File.pathSeparator + classpath);
        }
    }
}
