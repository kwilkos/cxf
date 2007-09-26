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
package org.apache.cxf.tools.java2wsdl.processor.internal.simple.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.FileWriterUtil;
import org.apache.cxf.tools.wsdlto.core.AbstractGenerator;

public abstract class AbstractSimpleGenerator extends AbstractGenerator {
    protected static final Logger LOG = LogUtils.getL7dLogger(AbstractSimpleGenerator.class);
    protected static final String TEMPLATE_BASE = "org/apache/cxf/tools" 
        + "/java2wsdl/processor/internal/simple/generator/template";

    public abstract boolean passthrough();

    public abstract void generate(ToolContext penv) throws ToolException;
    public void register(final ClassCollector collector, String packageName, String fileName) {
        
    }
    
    protected Writer parseOutputName(String packageName, String filename, String ext) throws ToolException {
        FileWriterUtil fw = null;
        Writer writer = null;

        if (wantToKeep() && isCollision(packageName, filename, ext)) {
            Message msg = new Message("SKIP_GEN", LOG, packageName + "." + filename + ext);
            LOG.log(Level.INFO, msg.toString());
            return null;
        }

        fw = new FileWriterUtil((String)env.get(ToolConstants.CFG_SOURCEDIR));
        try {
            writer = fw.getWriter(packageName, filename + ext);
        } catch (IOException ioe) {
            Message msg = new Message("FAIL_TO_WRITE_FILE", LOG, packageName + "." + filename + ext);
            throw new ToolException(msg, ioe);
        }

        return writer;
    }
    

}
