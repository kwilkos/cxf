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

import java.io.File;
import java.util.logging.Logger;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingConfiguration;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBindingConfiguration;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLConstants;
import org.apache.cxf.tools.java2wsdl.generator.AbstractGenerator;
import org.apache.cxf.tools.java2wsdl.generator.WSDLGeneratorFactory;
import org.apache.cxf.tools.java2wsdl.processor.internal.ServiceBuilder;
import org.apache.cxf.tools.java2wsdl.processor.internal.ServiceBuilderFactory;
import org.apache.cxf.tools.util.AnnotationUtil;

public class JavaToProcessor implements Processor {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToProcessor.class);
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
        builder.setTransportId(getTransportId());
        builder.setBus(getBus());
        builder.setBindingConfig(getBindingConfig());

        ServiceInfo service = builder.build();

        File output = getOutputFile(builder.getOutputFile(),
                                    service.getName().getLocalPart() + ".wsdl");

        WSDLGeneratorFactory factory = WSDLGeneratorFactory.getInstance();
        factory.setWSDLVersion(getWSDLVersion());

        AbstractGenerator generator = factory.newGenerator();
        generator.setAllowImports(context.containsKey(ToolConstants.CFG_CREATE_XSD_IMPORTS));
        generator.setServiceModel(service);
        generator.generate(output);
    }

    protected String getTransportId() {
        if (isSOAP12()) {
            return WSDLConstants.SOAP12_NAMESPACE;
        }
        return WSDLConstants.SOAP11_NAMESPACE;
    }
    
    protected BindingConfiguration getBindingConfig() {
        SoapBindingConfiguration bindingConfig = new SoapBindingConfiguration();
        if (isSOAP12()) {
            bindingConfig.setVersion(Soap12.getInstance());
            bindingConfig.setTransportURI(WSDLConstants.SOAP12_HTTP_TRANSPORT);
        } else {
            bindingConfig.setVersion(Soap11.getInstance());
        }
        return bindingConfig;
    }

    protected boolean isSOAP12() {
        if (!this.context.optionSet(ToolConstants.CFG_SOAP12)) {
            BindingType bType = getServiceClass().getAnnotation(BindingType.class);
            if (bType != null) {
                return SOAPBinding.SOAP12HTTP_BINDING.equals(bType.value());
            }
            return false;
        }
        return true;
    }

    protected File getOutputFile(File nameFromClz, String defaultOutputFile) {
        String output = (String) context.get(ToolConstants.CFG_OUTPUTFILE);
        String dir = (String)context.get(ToolConstants.CFG_OUTPUTDIR);
        if (dir == null) {
            dir = "./";
        }
        
        File result;
        if (output != null) {
            result = new File(output);
            if (!result.isAbsolute()) {
                result = new File(new File(dir), output);
            }
        } else {
            result = new File(new File(dir), defaultOutputFile);
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

    public Class<?> getServiceClass() {
        return AnnotationUtil.loadClass((String)context.get(ToolConstants.CFG_CLASSNAME),
                                        getClass().getClassLoader());
    }

    public WSDLConstants.WSDLVersion getWSDLVersion() {
        String version = (String) context.get(ToolConstants.CFG_WSDL_VERSION);
        WSDLConstants.WSDLVersion wsVersion = WSDLConstants.getVersion(version);
        if (wsVersion == WSDLConstants.WSDLVersion.UNKNOWN) {
            wsVersion = WSDLConstants.WSDLVersion.WSDL11;
        }
        return wsVersion;
    }

    public Bus getBus() {
        return BusFactory.getDefaultBus();
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
