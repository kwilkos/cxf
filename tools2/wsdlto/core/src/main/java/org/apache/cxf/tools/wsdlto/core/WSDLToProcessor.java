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

package org.apache.cxf.tools.wsdlto.core;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.FileWriterUtil;
import org.apache.velocity.app.Velocity;

public class WSDLToProcessor implements Processor {
    protected static final Logger LOG = LogUtils.getL7dLogger(WSDLToProcessor.class);
    protected static final String WSDL_FILE_NAME_EXT = ".wsdl";

    protected ToolContext context;

    protected Writer getOutputWriter(String newNameExt) throws ToolException {
        Writer writer = null;
        String newName = null;
        String outputDir;

        if (context.get(ToolConstants.CFG_OUTPUTFILE) != null) {
            newName = (String)context.get(ToolConstants.CFG_OUTPUTFILE);
        } else {
            String oldName = (String)context.get(ToolConstants.CFG_WSDLURL);
            int position = oldName.lastIndexOf("/");
            if (position < 0) {
                position = oldName.lastIndexOf("\\");
            }
            if (position >= 0) {
                oldName = oldName.substring(position + 1, oldName.length());
            }
            if (oldName.toLowerCase().indexOf(WSDL_FILE_NAME_EXT) >= 0) {
                newName = oldName.substring(0, oldName.length() - 5) + newNameExt + WSDL_FILE_NAME_EXT;
            } else {
                newName = oldName + newNameExt;
            }
        }
        if (context.get(ToolConstants.CFG_OUTPUTDIR) != null) {
            outputDir = (String)context.get(ToolConstants.CFG_OUTPUTDIR);
            if (!("/".equals(outputDir.substring(outputDir.length() - 1)) || "\\".equals(outputDir
                .substring(outputDir.length() - 1)))) {
                outputDir = outputDir + "/";
            }
        } else {
            outputDir = "./";
        }
        FileWriterUtil fw = new FileWriterUtil(outputDir);
        try {
            writer = fw.getWriter("", newName);
        } catch (IOException ioe) {
            org.apache.cxf.common.i18n.Message msg =
                new org.apache.cxf.common.i18n.Message("FAIL_TO_WRITE_FILE",
                                                       LOG,
                                                       context.get(ToolConstants.CFG_OUTPUTDIR)
                                                       + System.getProperty("file.seperator")
                                                       + newName);
            throw new ToolException(msg, ioe);
        }
        return writer;
    }

    private String getVelocityLogFile(String logfile) {
        String logdir = System.getProperty("user.home");
        if (logdir == null || logdir.length() == 0) {
            logdir = System.getProperty("user.dir");
        }
        return logdir + File.separator + logfile;
    }

    private void initVelocity() throws ToolException {
        try {
            Properties props = new Properties();
            String clzName = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
            props.put("resource.loader", "class");
            props.put("class.resource.loader.class", clzName);
            props.put("runtime.log", getVelocityLogFile("velocity.log"));

            Velocity.init(props);
        } catch (Exception e) {
            org.apache.cxf.common.i18n.Message msg =
                new org.apache.cxf.common.i18n.Message("FAIL_TO_INITIALIZE_VELOCITY_ENGINE",
                                                             LOG);
            LOG.log(Level.SEVERE, msg.toString());
            throw new ToolException(msg, e);
        }
    }

    private void init() throws ToolException {
        initVelocity();
        context.put(ClassCollector.class, new ClassCollector());
    }

    public void process() throws ToolException {
        init();
    }

    public void setEnvironment(ToolContext penv) {
        this.context = penv;
    }

    public ToolContext getEnvironment() {
        return this.context;
    }
    
    public ServiceInfo getServiceInfo() {
        return context.get(ServiceInfo.class);
    }
}
